package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.domain.Bookmark;
import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.exception.BookmarkNotFoundException;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
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

    @Override
    @Transactional
    public BookmarkResponse createBookmark(BookmarkCreateRequest request) {
        Bookmark bookmark = Bookmark.builder()
                .title(request.getTitle())
                .url(request.getUrl())
                .memo(request.getMemo())
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return BookmarkResponse.from(savedBookmark);
    }

    @Override
    public Page<BookmarkResponse> getBookmarks(String q, Pageable pageable) {
        Page<Bookmark> page = StringUtils.hasText(q)
                ? bookmarkRepository.findByTitleContainingIgnoreCaseOrUrlContainingIgnoreCase(q, q, pageable)
                : bookmarkRepository.findAll(pageable);

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
