package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.domain.Bookmark;
import io.github.minjoon98.bookmark.dto.request.BookmarkCreateRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.dto.request.BookmarkUpdateRequest;
import io.github.minjoon98.bookmark.exception.BookmarkNotFoundException;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceImplTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private BookmarkServiceImpl bookmarkServiceImpl;

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
        BookmarkResponse response = bookmarkServiceImpl.createBookmark(request);

        // then
        assertThat(response.getTitle()).isEqualTo("Google");
        assertThat(response.getUrl()).isEqualTo("https://www.google.com");
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("모든 북마크를 조회할 수 있다")
    void getAllBookmarks() {
        // given
        Bookmark bookmark1 = Bookmark.builder().title("Google").url("https://google.com").build();
        Bookmark bookmark2 = Bookmark.builder().title("GitHub").url("https://github.com").build();
        given(bookmarkRepository.findAll()).willReturn(Arrays.asList(bookmark1, bookmark2));

        // when
        List<BookmarkResponse> responses = bookmarkServiceImpl.getAllBookmarks();

        // then
        assertThat(responses).hasSize(2);
        verify(bookmarkRepository, times(1)).findAll();
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
        BookmarkResponse response = bookmarkServiceImpl.getBookmarkById(id);

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
        assertThatThrownBy(() -> bookmarkServiceImpl.getBookmarkById(id))
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
        BookmarkResponse response = bookmarkServiceImpl.updateBookmark(id, request);

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
        bookmarkServiceImpl.deleteBookmark(id);

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
        assertThatThrownBy(() -> bookmarkServiceImpl.deleteBookmark(id))
                .isInstanceOf(BookmarkNotFoundException.class);
    }

    @Test
    @DisplayName("키워드로 북마크를 검색할 수 있다")
    void searchBookmarks() {
        // given
        String keyword = "Git";
        Bookmark bookmark = Bookmark.builder().title("GitHub").url("https://github.com").build();
        given(bookmarkRepository.findByTitleContainingOrUrlContaining(keyword, keyword))
                .willReturn(Arrays.asList(bookmark));

        // when
        List<BookmarkResponse> responses = bookmarkServiceImpl.searchBookmarks(keyword);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("GitHub");
    }
}
