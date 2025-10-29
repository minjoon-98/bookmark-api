package io.github.minjoon98.bookmark.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TagUpsertRequest {

    @Schema(description = "추가할 태그 이름 목록", example = "[\"spring\", \"java\"]")
    @NotEmpty(message = "태그 목록은 비어 있을 수 없습니다")
    @Size(max = 20, message = "한 번에 최대 20개 태그만 추가할 수 있습니다")
    private List<@Size(min = 1, max = 50, message = "태그는 1~50자") String> names;

    public TagUpsertRequest(List<String> names) {
        this.names = names;
    }
}
