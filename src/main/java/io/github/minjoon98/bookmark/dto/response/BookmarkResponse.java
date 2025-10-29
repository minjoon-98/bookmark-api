package io.github.minjoon98.bookmark.dto.response;

import io.github.minjoon98.bookmark.domain.Bookmark;
import io.github.minjoon98.bookmark.domain.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "북마크 응답")
@Getter
@Builder
@AllArgsConstructor
public class BookmarkResponse {

    @Schema(description = "북마크 ID", example = "1")
    private Long id;

    @Schema(description = "북마크 제목", example = "Google")
    private String title;

    @Schema(description = "북마크 URL", example = "https://www.google.com")
    private String url;

    @Schema(description = "메모", example = "검색 엔진")
    private String memo;

    @Schema(description = "생성 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "태그 목록", example = "[\"spring\",\"java\"]")
    private List<String> tags;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
            .id(bookmark.getId())
            .title(bookmark.getTitle())
            .url(bookmark.getUrl())
            .memo(bookmark.getMemo())
            .createdAt(bookmark.getCreatedAt())
            .updatedAt(bookmark.getUpdatedAt())
            .tags(bookmark.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .toList())
            .build();
    }
}
