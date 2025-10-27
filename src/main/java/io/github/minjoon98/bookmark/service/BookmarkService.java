package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;

import java.util.List;

public interface BookmarkService {

    BookmarkResponse createBookmark(BookmarkCreateRequest request);

    List<BookmarkResponse> getAllBookmarks();

    BookmarkResponse getBookmarkById(Long id);

    BookmarkResponse updateBookmark(Long id, BookmarkUpdateRequest request);

    void deleteBookmark(Long id);

    List<BookmarkResponse> searchBookmarks(String keyword);
}
