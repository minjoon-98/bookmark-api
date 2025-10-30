package io.github.minjoon98.bookmark.controller;

import io.github.minjoon98.bookmark.docs.BookmarkApiDoc;
import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.MessageResponse;
import io.github.minjoon98.bookmark.service.BookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController implements BookmarkApiDoc {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<?> createBookmark(@Valid @RequestBody BookmarkCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookmarkService.createBookmark(request));
    }

    @GetMapping
    public ResponseEntity<?> getBookmarks(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getBookmarks(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookmarkById(@PathVariable Long id) {
        return ResponseEntity.ok(bookmarkService.getBookmarkById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBookmark(
            @PathVariable Long id,
            @Valid @RequestBody BookmarkUpdateRequest request) {
        return ResponseEntity.ok(bookmarkService.updateBookmark(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookmark(@PathVariable Long id) {
        bookmarkService.deleteBookmark(id);
        return ResponseEntity.ok(MessageResponse.of("북마크가 성공적으로 삭제되었습니다"));
    }

    @GetMapping("/by-tag")
    public ResponseEntity<?> getBookmarksByTag(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getBookmarksByTag(name, pageable));
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<?> addTags(
            @PathVariable Long id,
            @Valid @RequestBody TagUpsertRequest request) {
        return ResponseEntity.ok(bookmarkService.addTags(id, request));
    }

    @DeleteMapping("/{id}/tags/{tagName}")
    public ResponseEntity<?> removeTag(
            @PathVariable Long id,
            @PathVariable String tagName) {
        return ResponseEntity.ok(bookmarkService.removeTag(id, tagName));
    }
}
