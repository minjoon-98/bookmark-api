package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.entity.Bookmark;
import io.github.minjoon98.bookmark.entity.Tag;
import io.github.minjoon98.bookmark.entity.User;
import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.exception.BookmarkNotFoundException;
import io.github.minjoon98.bookmark.exception.UserNotFoundException;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
import io.github.minjoon98.bookmark.repository.TagRepository;
import io.github.minjoon98.bookmark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자 가져오기
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 북마크 소유자 검증
     */
    private void validateBookmarkOwner(Bookmark bookmark) {
        User currentUser = getCurrentUser();
        if (!bookmark.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("해당 북마크에 대한 접근 권한이 없습니다");
        }
    }

    /**
     * 북마크 생성 - 목록 캐시 전체 무효화
     * 새 항목이 목록에 포함되므로 모든 목록 캐시 제거
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = "bookmarksFirstPage", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksSearch", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksByTag", allEntries = true)
    })
    @Override
    @Transactional
    public BookmarkResponse createBookmark(BookmarkCreateRequest request) {
        User currentUser = getCurrentUser();

        Bookmark bookmark = Bookmark.builder()
            .title(request.getTitle())
            .url(request.getUrl())
            .memo(request.getMemo())
            .user(currentUser)
            .build();
        return BookmarkResponse.from(bookmarkRepository.save(bookmark));
    }

    /**
     * 목록 조회 - 조건부 캐싱
     * - 검색어 없고 page=0: 첫 페이지 캐싱 (홈 화면 체감 성능 개선, TTL 60초)
     * - 검색어 있고 len≥2, page≤2: 검색 결과 초기 페이지 캐싱 (TTL 30초)
     */
    @Caching(cacheable = {
        @Cacheable(
            cacheNames = "bookmarksFirstPage",
            keyGenerator = "pageableKeyGenerator",
            condition = "#q == null && #pageable.pageNumber == 0"
        ),
        @Cacheable(
            cacheNames = "bookmarksSearch",
            keyGenerator = "searchKeyGenerator",
            condition = "#q != null && #q.length() >= 2 && #pageable.pageNumber <= 2"
        )
    })
    @Override
    public Page<BookmarkResponse> getBookmarks(String q, Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<Bookmark> page = StringUtils.hasText(q)
            ? bookmarkRepository.findByUserAndTitleContainingIgnoreCaseOrUserAndUrlContainingIgnoreCase(
                currentUser, q, currentUser, q, pageable)
            : bookmarkRepository.findByUser(currentUser, pageable);
        return page.map(BookmarkResponse::from);
    }

    /**
     * 태그 추가 - 단건 캐시 + 목록 캐시 무효화
     * 태그 변경으로 태그별 조회 결과 변경
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = "bookmarkById", key = "#bookmarkId"),
        @CacheEvict(cacheNames = "bookmarksFirstPage", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksSearch", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksByTag", allEntries = true)
    })
    @Override
    @Transactional
    public BookmarkResponse addTags(Long bookmarkId, TagUpsertRequest request) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BookmarkNotFoundException(bookmarkId));
        validateBookmarkOwner(bookmark);

        for (String raw : request.getNames()) {
            String name = Tag.normalize(raw);
            if (!StringUtils.hasText(name)) continue;

            Tag tag = tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));

            bookmark.addTag(tag);
        }
        return BookmarkResponse.from(bookmark);
    }

    /**
     * 태그 제거 - 단건 캐시 + 목록 캐시 무효화
     * 태그 변경으로 태그별 조회 결과 변경, 고아 태그 자동 정리
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = "bookmarkById", key = "#bookmarkId"),
        @CacheEvict(cacheNames = "bookmarksFirstPage", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksSearch", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksByTag", allEntries = true)
    })
    @Override
    @Transactional
    public BookmarkResponse removeTag(Long bookmarkId, String tagName) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BookmarkNotFoundException(bookmarkId));
        validateBookmarkOwner(bookmark);

        String normalized = Tag.normalize(tagName);
        Tag tag = tagRepository.findByNameIgnoreCase(normalized)
            .orElseThrow(() -> new IllegalArgumentException("태그가 존재하지 않습니다: " + tagName));

        bookmark.removeTag(tag);

        // 사용처가 더 없으면 태그 정리(선택)
        if (tag.getBookmarkTags().isEmpty()) {
            tagRepository.delete(tag);
        }
        return BookmarkResponse.from(bookmark);
    }

    /**
     * 태그별 조회 - 초기 페이지만 캐싱 (page≤2, TTL 60초)
     * 특정 인기 태그에 대한 반복 조회 최적화
     */
    @Cacheable(
        cacheNames = "bookmarksByTag",
        keyGenerator = "tagSearchKeyGenerator",
        condition = "#pageable.pageNumber <= 2"
    )
    @Override
    public Page<BookmarkResponse> getBookmarksByTag(String tagName, Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Bookmark> page = bookmarkRepository.findDistinctByUserAndTagName(
            currentUser, Tag.normalize(tagName), pageable);
        return page.map(BookmarkResponse::from);
    }

    /**
     * 단건 조회 - 항상 캐싱 (TTL 10분)
     * 반복 조회가 많은 상세 페이지 최적화
     */
    @Cacheable(cacheNames = "bookmarkById", key = "#id")
    @Override
    public BookmarkResponse getBookmarkById(Long id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new BookmarkNotFoundException(id));
        validateBookmarkOwner(bookmark);
        return BookmarkResponse.from(bookmark);
    }

    /**
     * 북마크 수정 - 단건 캐시 + 목록 캐시 무효화
     * 제목/URL 변경 시 검색 결과에 영향
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = "bookmarkById", key = "#id"),
        @CacheEvict(cacheNames = "bookmarksFirstPage", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksSearch", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksByTag", allEntries = true)
    })
    @Override
    @Transactional
    public BookmarkResponse updateBookmark(Long id, BookmarkUpdateRequest request) {
        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new BookmarkNotFoundException(id));
        validateBookmarkOwner(bookmark);
        bookmark.update(request.getTitle(), request.getUrl(), request.getMemo());
        return BookmarkResponse.from(bookmark);
    }

    /**
     * 북마크 삭제 - 단건 캐시 + 목록 캐시 무효화
     * 목록에서 제거되므로 모든 캐시 갱신 필요
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = "bookmarkById", key = "#id"),
        @CacheEvict(cacheNames = "bookmarksFirstPage", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksSearch", allEntries = true),
        @CacheEvict(cacheNames = "bookmarksByTag", allEntries = true)
    })
    @Override
    @Transactional
    public void deleteBookmark(Long id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new BookmarkNotFoundException(id));
        validateBookmarkOwner(bookmark);
        bookmarkRepository.deleteById(id);
    }
}
