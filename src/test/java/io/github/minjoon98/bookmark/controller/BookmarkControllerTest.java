package io.github.minjoon98.bookmark.controller;

import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.exception.BookmarkNotFoundException;
import io.github.minjoon98.bookmark.service.BookmarkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("북마크 생성 API 테스트")
    void createBookmark() throws Exception {
        // given
        BookmarkCreateRequest request = new BookmarkCreateRequest("Google", "https://www.google.com", "검색");
        BookmarkResponse response = BookmarkResponse.builder()
                .id(1L)
                .title("Google")
                .url("https://www.google.com")
                .memo("검색")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(bookmarkService.createBookmark(any(BookmarkCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Google"));
    }

    @Test
    @DisplayName("북마크 목록 조회 API 테스트")
    void getAllBookmarks() throws Exception {
        // given
        List<BookmarkResponse> responses = Arrays.asList(
                BookmarkResponse.builder().id(1L).title("Google").url("https://google.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                BookmarkResponse.builder().id(2L).title("GitHub").url("https://github.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        given(bookmarkService.getAllBookmarks()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Google"))
                .andExpect(jsonPath("$[1].title").value("GitHub"));
    }

    @Test
    @DisplayName("북마크 단건 조회 API 테스트")
    void getBookmarkById() throws Exception {
        // given
        Long id = 1L;
        BookmarkResponse response = BookmarkResponse.builder()
                .id(id)
                .title("Google")
                .url("https://google.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(bookmarkService.getBookmarkById(id)).willReturn(response);

        // when & then
        mockMvc.perform(get("/bookmarks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Google"));
    }

    @Test
    @DisplayName("존재하지 않는 북마크 조회 시 404 반환")
    void getBookmarkByIdNotFound() throws Exception {
        // given
        Long id = 999L;
        given(bookmarkService.getBookmarkById(id)).willThrow(new BookmarkNotFoundException(id));

        // when & then
        mockMvc.perform(get("/bookmarks/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("북마크 수정 API 테스트")
    void updateBookmark() throws Exception {
        // given
        Long id = 1L;
        BookmarkUpdateRequest request = new BookmarkUpdateRequest("Updated Title", null, null);
        BookmarkResponse response = BookmarkResponse.builder()
                .id(id)
                .title("Updated Title")
                .url("https://google.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(bookmarkService.updateBookmark(eq(id), any(BookmarkUpdateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/bookmarks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("북마크 삭제 API 테스트")
    void deleteBookmark() throws Exception {
        // given
        Long id = 1L;
        doNothing().when(bookmarkService).deleteBookmark(id);

        // when & then
        mockMvc.perform(delete("/bookmarks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("북마크가 성공적으로 삭제되었습니다"));

        verify(bookmarkService, times(1)).deleteBookmark(id);
    }

    @Test
    @DisplayName("검색 API 테스트")
    void searchBookmarks() throws Exception {
        // given
        String keyword = "Git";
        List<BookmarkResponse> responses = Arrays.asList(
                BookmarkResponse.builder().id(1L).title("GitHub").url("https://github.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        given(bookmarkService.searchBookmarks(keyword)).willReturn(responses);

        // when & then
        mockMvc.perform(get("/bookmarks")
                        .param("search", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("GitHub"));
    }

    @Test
    @DisplayName("유효성 검증 실패 시 400 반환")
    void createBookmarkWithInvalidData() throws Exception {
        // given
        BookmarkCreateRequest request = new BookmarkCreateRequest("", "", "메모");

        // when & then
        mockMvc.perform(post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
