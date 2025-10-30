package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.entity.User;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
import io.github.minjoon98.bookmark.repository.TagRepository;
import io.github.minjoon98.bookmark.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 캐싱 동작 검증 테스트
 *
 * <p>@SpyBean을 사용하여 실제 Repository 호출 횟수를 추적하고,
 * 캐시 적중/무효화가 제대로 작동하는지 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookmarkServiceCacheTest {

    @Autowired
    private BookmarkService bookmarkService;

    @MockitoSpyBean
    private BookmarkRepository bookmarkRepository;

    @MockitoSpyBean
    private TagRepository tagRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = userRepository.save(User.builder()
                .email("cache-test@example.com")
                .password("password")
                .build());

        // SecurityContext 설정 (직접 설정)
        org.springframework.security.core.Authentication authentication =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                testUser.getId().toString(), null, List.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // 캐시 초기화
        clearAllCaches();
    }

    @Test
    @DisplayName("단건 조회 시 캐시가 적용되어 두 번째 조회는 DB를 호출하지 않는다")
    void cache_hit_on_getBookmarkById() {
        // given - 북마크 생성 (캐시 무효화로 인해 별도 트랜잭션에서 실행)
        BookmarkResponse created = bookmarkService.createBookmark(
            new BookmarkCreateRequest("Test", "https://test.com", "memo")
        );
        Long id = created.getId();

        // 캐시 초기화 (생성 시 무효화 영향 제거)
        clearAllCaches();
        clearInvocations(bookmarkRepository);

        // when - 동일 ID로 2회 조회
        BookmarkResponse first = bookmarkService.getBookmarkById(id);
        BookmarkResponse second = bookmarkService.getBookmarkById(id);

        // then - Repository는 1회만 호출 (두 번째는 캐시 히트)
        verify(bookmarkRepository, times(1)).findById(id);
        assertThat(first.getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("전체 목록 첫 페이지(page=0, 검색어 없음) 조회 시 캐시가 적용된다")
    void cache_hit_on_first_page() {
        // given
        bookmarkService.createBookmark(new BookmarkCreateRequest("A", "https://a.com", ""));
        bookmarkService.createBookmark(new BookmarkCreateRequest("B", "https://b.com", ""));

        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        clearAllCaches();
        clearInvocations(bookmarkRepository);

        // when - 동일 조건으로 2회 조회
        Page<BookmarkResponse> first = bookmarkService.getBookmarks(null, pageable);
        Page<BookmarkResponse> second = bookmarkService.getBookmarks(null, pageable);

        // then - Repository는 1회만 호출
        verify(bookmarkRepository, times(1)).findByUser(eq(testUser), any(Pageable.class));
        assertThat(first.getTotalElements()).isEqualTo(second.getTotalElements());
    }

    @Test
    @DisplayName("검색 조회(len≥2, page≤2) 시 캐시가 적용된다")
    void cache_hit_on_search() {
        // given
        bookmarkService.createBookmark(new BookmarkCreateRequest("GitHub", "https://github.com", ""));
        bookmarkService.createBookmark(new BookmarkCreateRequest("GitLab", "https://gitlab.com", ""));

        Pageable pageable = PageRequest.of(0, 20);
        String keyword = "git";
        clearAllCaches();
        clearInvocations(bookmarkRepository);

        // when - 동일 검색어로 2회 조회
        Page<BookmarkResponse> first = bookmarkService.getBookmarks(keyword, pageable);
        Page<BookmarkResponse> second = bookmarkService.getBookmarks(keyword, pageable);

        // then - Repository는 1회만 호출
        verify(bookmarkRepository, times(1))
            .findByUserAndTitleContainingIgnoreCaseOrUserAndUrlContainingIgnoreCase(
                    eq(testUser), eq(keyword), eq(testUser), eq(keyword), any(Pageable.class));
        assertThat(first.getTotalElements()).isEqualTo(second.getTotalElements());
    }

    @Test
    @DisplayName("북마크 생성 시 목록 캐시가 무효화된다")
    void cache_evict_on_create() {
        // given - 첫 페이지 조회하여 캐시 생성
        Pageable pageable = PageRequest.of(0, 20);
        bookmarkService.getBookmarks(null, pageable);
        clearInvocations(bookmarkRepository);

        // when - 새 북마크 생성
        bookmarkService.createBookmark(new BookmarkCreateRequest("New", "https://new.com", ""));

        // 캐시가 무효화되었으므로 다시 조회 시 DB 호출
        bookmarkService.getBookmarks(null, pageable);

        // then - Repository가 다시 호출됨 (캐시 미스)
        verify(bookmarkRepository, times(1)).findByUser(eq(testUser), any(Pageable.class));
    }

    @Test
    @DisplayName("북마크 수정 시 단건 캐시와 목록 캐시가 모두 무효화된다")
    void cache_evict_on_update() {
        // given - 북마크 생성 및 조회하여 캐시 적재
        BookmarkResponse created = bookmarkService.createBookmark(
            new BookmarkCreateRequest("Original", "https://original.com", "")
        );
        Long id = created.getId();

        clearAllCaches();
        bookmarkService.getBookmarkById(id); // 단건 캐시 생성
        bookmarkService.getBookmarks(null, PageRequest.of(0, 20)); // 목록 캐시 생성
        clearInvocations(bookmarkRepository);

        // when - 북마크 수정 (내부적으로 findById 1회 호출)
        bookmarkService.updateBookmark(id, new BookmarkUpdateRequest("Updated", null, null));

        // 캐시가 무효화되었으므로 재조회 시 DB 호출
        bookmarkService.getBookmarkById(id);
        bookmarkService.getBookmarks(null, PageRequest.of(0, 20));

        // then - Repository가 각각 재호출됨 (update 시 1회 + 재조회 시 1회 = 2회)
        verify(bookmarkRepository, times(2)).findById(id);
        verify(bookmarkRepository, times(1)).findByUser(eq(testUser), any(Pageable.class));
    }

    @Test
    @DisplayName("북마크 삭제 시 모든 캐시가 무효화된다")
    void cache_evict_on_delete() {
        // given
        BookmarkResponse created = bookmarkService.createBookmark(
            new BookmarkCreateRequest("ToDelete", "https://delete.com", "")
        );
        Long id = created.getId();

        clearAllCaches();
        bookmarkService.getBookmarkById(id);
        clearInvocations(bookmarkRepository);

        // when - 삭제
        bookmarkService.deleteBookmark(id);

        // then - 삭제 후 재조회 시 예외 발생 (캐시에서 제거됨)
        try {
            bookmarkService.getBookmarkById(id);
        } catch (Exception e) {
            // 예외 발생 = DB 조회 시도 = 캐시 무효화 성공
            // deleteBookmark에서 1회, getBookmarkById에서 1회 = 총 2회
            verify(bookmarkRepository, times(2)).findById(id);
        }
    }

    @Test
    @DisplayName("태그 추가 시 단건 및 목록 캐시가 무효화된다")
    void cache_evict_on_addTags() {
        // given
        BookmarkResponse created = bookmarkService.createBookmark(
            new BookmarkCreateRequest("TagTest", "https://tag.com", "")
        );
        Long id = created.getId();

        clearAllCaches();
        bookmarkService.getBookmarkById(id);
        clearInvocations(bookmarkRepository);

        // when - 태그 추가 (내부적으로 findById 1회 호출)
        bookmarkService.addTags(id, new TagUpsertRequest(List.of("spring", "java")));

        // 캐시 무효화 확인
        bookmarkService.getBookmarkById(id);

        // then - Repository 재호출 (addTags 시 1회 + 재조회 시 1회 = 2회)
        verify(bookmarkRepository, times(2)).findById(id);
    }

    @Test
    @DisplayName("태그 제거 시 캐시가 무효화된다")
    void cache_evict_on_removeTag() {
        // given
        BookmarkResponse created = bookmarkService.createBookmark(
            new BookmarkCreateRequest("TagTest", "https://tag.com", "")
        );
        Long id = created.getId();
        bookmarkService.addTags(id, new TagUpsertRequest(List.of("spring")));

        clearAllCaches();
        bookmarkService.getBookmarkById(id);
        clearInvocations(bookmarkRepository);

        // when - 태그 제거 (내부적으로 findById 1회 호출)
        bookmarkService.removeTag(id, "spring");

        // 캐시 무효화 확인
        bookmarkService.getBookmarkById(id);

        // then - Repository 재호출 (removeTag 시 1회 + 재조회 시 1회 = 2회)
        verify(bookmarkRepository, times(2)).findById(id);
    }

    @Test
    @DisplayName("page > 0인 경우 전체 목록은 캐싱되지 않는다 (조건부 캐싱)")
    void no_cache_for_non_first_page() {
        // given
        bookmarkService.createBookmark(new BookmarkCreateRequest("A", "https://a.com", ""));

        Pageable page1 = PageRequest.of(1, 20); // page=1은 캐싱 안 됨
        clearAllCaches();
        clearInvocations(bookmarkRepository);

        // when - 동일 조건으로 2회 조회
        bookmarkService.getBookmarks(null, page1);
        bookmarkService.getBookmarks(null, page1);

        // then - 캐싱 안 되므로 2회 호출
        verify(bookmarkRepository, times(2)).findByUser(eq(testUser), any(Pageable.class));
    }

    @Test
    @DisplayName("태그별 조회(page≤2)는 캐싱된다")
    void cache_hit_on_getBookmarksByTag() {
        // given
        BookmarkResponse created = bookmarkService.createBookmark(
            new BookmarkCreateRequest("Spring", "https://spring.io", "")
        );
        bookmarkService.addTags(created.getId(), new TagUpsertRequest(List.of("framework")));

        Pageable pageable = PageRequest.of(0, 20);
        clearAllCaches();
        clearInvocations(bookmarkRepository);

        // when - 동일 태그로 2회 조회
        bookmarkService.getBookmarksByTag("framework", pageable);
        bookmarkService.getBookmarksByTag("framework", pageable);

        // then - 1회만 호출
        verify(bookmarkRepository, times(1))
            .findDistinctByUserAndTagName(eq(testUser), eq("framework"), any(Pageable.class));
    }

    /**
     * 모든 캐시 초기화
     */
    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}
