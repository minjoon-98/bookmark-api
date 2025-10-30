package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkService {

    BookmarkResponse createBookmark(BookmarkCreateRequest request);

    Page<BookmarkResponse> getBookmarks(String q, Pageable pageable);

    BookmarkResponse addTags(Long bookmarkId, TagUpsertRequest request);

    BookmarkResponse removeTag(Long bookmarkId, String tagName);

    Page<BookmarkResponse> getBookmarksByTag(String tagName, Pageable pageable);

    BookmarkResponse getBookmarkById(Long id);

    BookmarkResponse updateBookmark(Long id, BookmarkUpdateRequest request);

    void deleteBookmark(Long id);
}
