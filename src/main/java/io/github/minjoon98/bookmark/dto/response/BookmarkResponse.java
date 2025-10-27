package io.github.minjoon98.bookmark.dto.response;

import io.github.minjoon98.bookmark.domain.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkResponse {

    private Long id;
    private String title;
    private String url;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .title(bookmark.getTitle())
                .url(bookmark.getUrl())
                .memo(bookmark.getMemo())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }
}
