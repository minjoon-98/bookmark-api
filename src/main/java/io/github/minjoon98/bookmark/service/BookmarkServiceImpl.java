package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.domain.Bookmark;
import io.github.minjoon98.bookmark.domain.Tag;
import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.exception.BookmarkNotFoundException;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
import io.github.minjoon98.bookmark.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public BookmarkResponse createBookmark(BookmarkCreateRequest request) {
        Bookmark bookmark = Bookmark.builder()
            .title(request.getTitle())
            .url(request.getUrl())
            .memo(request.getMemo())
            .build();
        return BookmarkResponse.from(bookmarkRepository.save(bookmark));
    }

    @Override
    public Page<BookmarkResponse> getBookmarks(String q, Pageable pageable) {
        Page<Bookmark> page = StringUtils.hasText(q)
            ? bookmarkRepository.findByTitleContainingIgnoreCaseOrUrlContainingIgnoreCase(q, q, pageable)
            : bookmarkRepository.findAll(pageable);
        return page.map(BookmarkResponse::from);
    }

    @Override
    @Transactional
    public BookmarkResponse addTags(Long bookmarkId, TagUpsertRequest request) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BookmarkNotFoundException(bookmarkId));

        for (String raw : request.getNames()) {
            String name = Tag.normalize(raw);
            if (!StringUtils.hasText(name)) continue;

            Tag tag = tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));

            bookmark.addTag(tag);
        }
        return BookmarkResponse.from(bookmark);
    }

    @Override
    @Transactional
    public BookmarkResponse removeTag(Long bookmarkId, String tagName) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BookmarkNotFoundException(bookmarkId));

        String normalized = Tag.normalize(tagName);
        Tag tag = tagRepository.findByNameIgnoreCase(normalized)
            .orElseThrow(() -> new IllegalArgumentException("태그가 존재하지 않습니다: " + tagName));

        bookmark.removeTag(tag);

        // 사용처가 더 없으면 태그 정리(선택)
        if (tag.getBookmarks().isEmpty()) {
            tagRepository.delete(tag);
        }
        return BookmarkResponse.from(bookmark);
    }

    @Override
    public Page<BookmarkResponse> getBookmarksByTag(String tagName, Pageable pageable) {
        Page<Bookmark> page = bookmarkRepository.findDistinctByTags_NameIgnoreCase(
            Tag.normalize(tagName), pageable);
        return page.map(BookmarkResponse::from);
    }

    @Override
    public BookmarkResponse getBookmarkById(Long id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new BookmarkNotFoundException(id));
        return BookmarkResponse.from(bookmark);
    }

    @Override
    @Transactional
    public BookmarkResponse updateBookmark(Long id, BookmarkUpdateRequest request) {
        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new BookmarkNotFoundException(id));
        bookmark.update(request.getTitle(), request.getUrl(), request.getMemo());
        return BookmarkResponse.from(bookmark);
    }

    @Override
    @Transactional
    public void deleteBookmark(Long id) {
        if (!bookmarkRepository.existsById(id)) {
            throw new BookmarkNotFoundException(id);
        }
        bookmarkRepository.deleteById(id);
    }
}
