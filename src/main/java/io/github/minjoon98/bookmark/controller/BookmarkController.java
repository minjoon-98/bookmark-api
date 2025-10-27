package io.github.minjoon98.bookmark.controller;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.dto.response.ErrorResponse;
import io.github.minjoon98.bookmark.dto.response.MessageResponse;
import io.github.minjoon98.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookmark", description = "북마크 관리 API")
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 생성", description = "새로운 북마크를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "북마크 생성 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 북마크 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookmarkCreateRequest.class)))
            @Valid @RequestBody BookmarkCreateRequest request) {
        BookmarkResponse response = bookmarkService.createBookmark(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "북마크 목록 조회", description = "전체 북마크 목록을 조회하거나 키워드로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getAllBookmarks(
            @Parameter(description = "검색 키워드 (제목 또는 URL)", required = false)
            @RequestParam(required = false) String search) {
        List<BookmarkResponse> responses;
        if (search != null && !search.isEmpty()) {
            responses = bookmarkService.searchBookmarks(search);
        } else {
            responses = bookmarkService.getAllBookmarks();
        }
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "북마크 상세 조회", description = "특정 ID의 북마크 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookmarkResponse> getBookmarkById(
            @Parameter(description = "북마크 ID", required = true)
            @PathVariable Long id) {
        BookmarkResponse response = bookmarkService.getBookmarkById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 수정", description = "특정 ID의 북마크 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookmarkResponse> updateBookmark(
            @Parameter(description = "북마크 ID", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 북마크 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookmarkUpdateRequest.class)))
            @Valid @RequestBody BookmarkUpdateRequest request) {
        BookmarkResponse response = bookmarkService.updateBookmark(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 삭제", description = "특정 ID의 북마크를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteBookmark(
            @Parameter(description = "북마크 ID", required = true)
            @PathVariable Long id) {
        bookmarkService.deleteBookmark(id);
        return ResponseEntity.ok(MessageResponse.of("북마크가 성공적으로 삭제되었습니다"));
    }
}
