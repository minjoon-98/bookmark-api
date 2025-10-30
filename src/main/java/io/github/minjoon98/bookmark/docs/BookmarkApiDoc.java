package io.github.minjoon98.bookmark.docs;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Bookmark", description = "북마크 API")
@SecurityRequirement(name = "bearerAuth")
public interface BookmarkApiDoc {

    @Operation(summary = "북마크 생성", description = "새로운 북마크를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    default ResponseEntity<?> createBookmark(BookmarkCreateRequest request) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "북마크 목록 조회", description = "본인의 북마크 목록을 조회합니다. 검색, 페이지네이션, 정렬을 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    default ResponseEntity<?> getBookmarks(
            @Parameter(description = "검색 키워드 (제목, URL)") String search,
            @Parameter(description = "페이지 정보") Pageable pageable) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "북마크 상세 조회", description = "특정 북마크의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    default ResponseEntity<?> getBookmarkById(
            @Parameter(description = "북마크 ID") Long id) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "북마크 수정", description = "북마크 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    default ResponseEntity<?> updateBookmark(
            @Parameter(description = "북마크 ID") Long id,
            BookmarkUpdateRequest request) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "북마크 삭제", description = "북마크를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    default ResponseEntity<?> deleteBookmark(
            @Parameter(description = "북마크 ID") Long id) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "태그별 북마크 조회", description = "특정 태그가 포함된 북마크 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    default ResponseEntity<?> getBookmarksByTag(
            @Parameter(description = "태그 이름") String name,
            @Parameter(description = "페이지 정보") Pageable pageable) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "태그 추가", description = "북마크에 태그를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    default ResponseEntity<?> addTags(
            @Parameter(description = "북마크 ID") Long id,
            TagUpsertRequest request) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "태그 제거", description = "북마크에서 태그를 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제거 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    default ResponseEntity<?> removeTag(
            @Parameter(description = "북마크 ID") Long id,
            @Parameter(description = "태그 이름") String tagName) {
        throw new UnsupportedOperationException("Doc only");
    }
}
