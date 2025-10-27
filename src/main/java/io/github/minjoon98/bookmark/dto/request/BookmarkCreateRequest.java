package io.github.minjoon98.bookmark.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "북마크 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkCreateRequest {

    @Schema(description = "북마크 제목", example = "Google", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @Schema(description = "북마크 URL", example = "https://www.google.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "URL은 필수입니다")
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String url;

    @Schema(description = "메모", example = "검색 엔진", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다")
    private String memo;
}
