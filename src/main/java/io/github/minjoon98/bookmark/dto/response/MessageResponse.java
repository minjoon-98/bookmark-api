package io.github.minjoon98.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "메시지 응답")
@Getter
@AllArgsConstructor
public class MessageResponse {

    @Schema(description = "응답 메시지", example = "북마크가 성공적으로 삭제되었습니다")
    private String message;

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
