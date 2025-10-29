package io.github.minjoon98.bookmark.controller;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
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
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Bookmark", description = "북마크 관리 API")
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 등록", description = "새로운 북마크를 생성합니다.")
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

    @Operation(
            summary = "북마크 목록 조회",
            description = "전체 북마크 목록을 조회하거나 키워드로 검색할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public Page<BookmarkResponse> getBookmarks(
            @Parameter(description = "검색 키워드 (선택). 제목 또는 URL에서 부분 일치 검색 (대소문자 무시)")
            @RequestParam(required = false, name = "search") String search,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @Min(0) @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "페이지 크기 (한 페이지에 포함될 항목 수)")
            @Min(1) @RequestParam(required = false, defaultValue = "20") Integer size,

            @Parameter(description = "정렬 기준. 형식: `필드명,방향`. 여러 개 지정 가능. 허용 필드: createdAt, updatedAt, title, url")
            @RequestParam(required = false, defaultValue = "createdAt,desc") String[] sort) {

        // Pageable 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));

        // 정렬 필드 화이트리스트 검증
        pageable = sanitizeSort(pageable, Set.of("createdAt", "updatedAt", "title", "url"));

        return bookmarkService.getBookmarks(search, pageable);
    }

    @Operation(summary = "태그로 북마크 조회", description = "특정 태그를 가진 북마크를 페이지네이션으로 조회합니다.")
    @GetMapping("/by-tag")
    public Page<BookmarkResponse> getByTag(
        @Parameter(description = "태그 이름", required = true, example = "spring")
        @RequestParam String name,
        @Parameter(description = "페이지 번호(0부터)") @Min(0)
        @RequestParam(defaultValue = "0") Integer page,
        @Parameter(description = "페이지 크기") @Min(1)
        @RequestParam(defaultValue = "20") Integer size,
        @Parameter(description = "정렬(필드,방향) createdAt,desc 형태. 허용: createdAt, updatedAt, title, url")
        @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        pageable = sanitizeSort(pageable, Set.of("createdAt", "updatedAt", "title", "url"));
        return bookmarkService.getBookmarksByTag(name, pageable);
    }

    @Operation(summary = "북마크에 태그 추가", description = "지정한 북마크에 태그를 추가합니다(중복 무시).")
    @PostMapping("/{id}/tags")
    public ResponseEntity<BookmarkResponse> addTags(
        @PathVariable Long id,
        @Valid @RequestBody TagUpsertRequest request
    ) {
        return ResponseEntity.ok(bookmarkService.addTags(id, request));
    }

    @Operation(summary = "북마크에서 태그 제거", description = "지정한 북마크에서 태그를 제거합니다.")
    @DeleteMapping("/{id}/tags/{tagName}")
    public ResponseEntity<BookmarkResponse> removeTag(
        @PathVariable Long id,
        @PathVariable String tagName
    ) {
        return ResponseEntity.ok(bookmarkService.removeTag(id, tagName));
    }

    /**
     * sort 파라미터 배열을 Sort.Order 리스트로 변환
     */
    private Sort.Order[] parseSort(String[] sort) {
        return java.util.Arrays.stream(sort)
                .map(s -> {
                    String[] parts = s.split(",");
                    String property = parts[0];
                    Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
                    return new Sort.Order(direction, property);
                })
                .toArray(Sort.Order[]::new);
    }

    /**
     * 정렬 필드를 화이트리스트로 검증하여 안전한 Pageable 반환
     */
    private Pageable sanitizeSort(Pageable pageable, Set<String> allowedFields) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Sort safeSort = Sort.by(
                pageable.getSort().stream()
                        .map(order -> allowedFields.contains(order.getProperty())
                                ? order
                                : new Sort.Order(order.getDirection(), "createdAt"))
                        .toList()
        );

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
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
