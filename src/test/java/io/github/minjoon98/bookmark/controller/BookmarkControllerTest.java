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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(controllers = BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
    @DisplayName("북마크 목록 조회 API 테스트 (기본값)")
    void getBookmarks() throws Exception {
        // given
        List<BookmarkResponse> content = Arrays.asList(
                BookmarkResponse.builder().id(1L).title("Google").url("https://google.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                BookmarkResponse.builder().id(2L).title("GitHub").url("https://github.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        Page<BookmarkResponse> page = new PageImpl<>(content, PageRequest.of(0, 20), 2);
        given(bookmarkService.getBookmarks(eq(null), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Google"))
                .andExpect(jsonPath("$.content[1].title").value("GitHub"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
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
        List<BookmarkResponse> content = Arrays.asList(
                BookmarkResponse.builder().id(1L).title("GitHub").url("https://github.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        Page<BookmarkResponse> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        given(bookmarkService.getBookmarks(eq(keyword), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/bookmarks").param("search", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("GitHub"))
                .andExpect(jsonPath("$.totalElements").value(1));
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

    @Test
    @DisplayName("페이지네이션과 정렬 적용된 북마크 목록 조회 API 테스트")
    void getBookmarksWithPaginationAndSort() throws Exception {
        // given
        List<BookmarkResponse> content = Arrays.asList(
                BookmarkResponse.builder().id(1L).title("Google").url("https://google.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                BookmarkResponse.builder().id(2L).title("GitHub").url("https://github.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        Page<BookmarkResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);
        given(bookmarkService.getBookmarks(eq(null), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/bookmarks")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Google"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("페이지네이션 적용된 검색 API 테스트")
    void searchBookmarksWithPagination() throws Exception {
        // given
        String keyword = "Git";
        List<BookmarkResponse> content = Arrays.asList(
                BookmarkResponse.builder().id(1L).title("GitHub").url("https://github.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        Page<BookmarkResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), 1);
        given(bookmarkService.getBookmarks(eq(keyword), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/bookmarks")
                        .param("search", keyword)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("GitHub"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("태그별 조회 API 테스트")
    void getByTag() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Page<BookmarkResponse> page = new PageImpl<>(
            List.of(BookmarkResponse.builder()
                .id(1L)
                .title("Google")
                .url("https://google.com")
                .createdAt(now)
                .updatedAt(now)
                .build()),
            PageRequest.of(0, 20), 1
        );
        given(bookmarkService.getBookmarksByTag(eq("spring"), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/bookmarks/by-tag").param("name", "spring"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Google"));
    }

    @Test
    @DisplayName("태그 추가/제거 API 테스트")
    void addAndRemoveTags() throws Exception {
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();
        BookmarkResponse withTags = BookmarkResponse.builder()
            .id(id)
            .title("Google")
            .url("https://google.com")
            .tags(List.of("java", "spring"))
            .createdAt(now)
            .updatedAt(now)
            .build();
        BookmarkResponse afterRemove = BookmarkResponse.builder()
            .id(id)
            .title("Google")
            .url("https://google.com")
            .tags(List.of("spring"))
            .createdAt(now)
            .updatedAt(now)
            .build();

        given(bookmarkService.addTags(eq(id), any())).willReturn(withTags);
        given(bookmarkService.removeTag(id, "java")).willReturn(afterRemove);

        // add
        mockMvc.perform(post("/bookmarks/{id}/tags", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"names\":[\"spring\",\"Java\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tags.length()").value(2))
            .andExpect(jsonPath("$.tags[0]").value("java"))
            .andExpect(jsonPath("$.tags[1]").value("spring"));

        // remove
        mockMvc.perform(delete("/bookmarks/{id}/tags/{tag}", id, "java"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tags.length()").value(1))
            .andExpect(jsonPath("$.tags[0]").value("spring"));
    }
}
