package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.entity.Bookmark;
import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.exception.BookmarkNotFoundException;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
import io.github.minjoon98.bookmark.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    @Test
    @DisplayName("북마크를 생성할 수 있다")
    void createBookmark() {
        // given
        BookmarkCreateRequest request = new BookmarkCreateRequest("Google", "https://www.google.com", "검색");
        Bookmark bookmark = Bookmark.builder()
                .title("Google")
                .url("https://www.google.com")
                .memo("검색")
                .build();

        given(bookmarkRepository.save(any(Bookmark.class))).willReturn(bookmark);

        // when
        BookmarkResponse response = bookmarkService.createBookmark(request);

        // then
        assertThat(response.getTitle()).isEqualTo("Google");
        assertThat(response.getUrl()).isEqualTo("https://www.google.com");
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("모든 북마크를 조회할 수 있다")
    void getBookmarks() {
        // given
        Bookmark bookmark1 = Bookmark.builder().title("Google").url("https://google.com").build();
        Bookmark bookmark2 = Bookmark.builder().title("GitHub").url("https://github.com").build();
        Page<Bookmark> page = new PageImpl<>(Arrays.asList(bookmark1, bookmark2), PageRequest.of(0, 20), 2);
        given(bookmarkRepository.findAll(any(Pageable.class))).willReturn(page);

        // when
        Page<BookmarkResponse> responses = bookmarkService.getBookmarks(null, PageRequest.of(0, 20));

        // then
        assertThat(responses.getContent()).hasSize(2);
        verify(bookmarkRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("ID로 북마크를 조회할 수 있다")
    void getBookmarkById() {
        // given
        Long id = 1L;
        Bookmark bookmark = Bookmark.builder()
                .title("Google")
                .url("https://google.com")
                .build();
        given(bookmarkRepository.findById(id)).willReturn(Optional.of(bookmark));

        // when
        BookmarkResponse response = bookmarkService.getBookmarkById(id);

        // then
        assertThat(response.getTitle()).isEqualTo("Google");
        verify(bookmarkRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getBookmarkByIdNotFound() {
        // given
        Long id = 999L;
        given(bookmarkRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.getBookmarkById(id))
                .isInstanceOf(BookmarkNotFoundException.class);
    }

    @Test
    @DisplayName("북마크를 수정할 수 있다")
    void updateBookmark() {
        // given
        Long id = 1L;
        BookmarkUpdateRequest request = new BookmarkUpdateRequest("Updated Title", null, "Updated memo");
        Bookmark bookmark = Bookmark.builder()
                .title("Original Title")
                .url("https://example.com")
                .memo("Original memo")
                .build();

        given(bookmarkRepository.findById(id)).willReturn(Optional.of(bookmark));

        // when
        BookmarkResponse response = bookmarkService.updateBookmark(id, request);

        // then
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getMemo()).isEqualTo("Updated memo");
        verify(bookmarkRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("북마크를 삭제할 수 있다")
    void deleteBookmark() {
        // given
        Long id = 1L;
        given(bookmarkRepository.existsById(id)).willReturn(true);
        doNothing().when(bookmarkRepository).deleteById(id);

        // when
        bookmarkService.deleteBookmark(id);

        // then
        verify(bookmarkRepository, times(1)).existsById(id);
        verify(bookmarkRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("존재하지 않는 북마크 삭제 시 예외가 발생한다")
    void deleteBookmarkNotFound() {
        // given
        Long id = 999L;
        given(bookmarkRepository.existsById(id)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookmarkService.deleteBookmark(id))
                .isInstanceOf(BookmarkNotFoundException.class);
    }

    @Test
    @DisplayName("키워드로 북마크를 검색할 수 있다 (대소문자 무시)")
    void searchBookmarks() {
        // given
        String keyword = "git";
        Bookmark bookmark = Bookmark.builder().title("GitHub").url("https://github.com").build();
        Page<Bookmark> page = new PageImpl<>(Arrays.asList(bookmark), PageRequest.of(0, 20), 1);
        given(bookmarkRepository.findByTitleContainingIgnoreCaseOrUrlContainingIgnoreCase(
                eq(keyword), eq(keyword), any(Pageable.class)))
                .willReturn(page);

        // when
        Page<BookmarkResponse> responses = bookmarkService.getBookmarks(keyword, PageRequest.of(0, 20));

        // then
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("GitHub");
    }
}
