package io.github.minjoon98.bookmark.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkUpdateRequest {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String url;

    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다")
    private String memo;
}
