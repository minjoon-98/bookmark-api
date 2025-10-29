package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.entity.Bookmark;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    @DisplayName("북마크를 저장하고 조회할 수 있다")
    void saveAndFindBookmark() {
        // given
        Bookmark bookmark = Bookmark.builder()
                .title("Google")
                .url("https://www.google.com")
                .memo("검색 엔진")
                .build();

        // when
        Bookmark saved = bookmarkRepository.save(bookmark);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Google");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 북마크를 조회할 수 있다")
    void findById() {
        // given
        Bookmark bookmark = Bookmark.builder()
                .title("GitHub")
                .url("https://github.com")
                .memo("코드 저장소")
                .build();
        Bookmark saved = bookmarkRepository.save(bookmark);

        // when
        Optional<Bookmark> found = bookmarkRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("GitHub");
    }

    @Test
    @DisplayName("제목이나 URL로 북마크를 검색할 수 있다 (대소문자 무시)")
    void searchByTitleOrUrl() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .title("Google")
                .url("https://www.google.com")
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .title("GitHub")
                .url("https://github.com")
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .title("Stack Overflow")
                .url("https://stackoverflow.com")
                .build());

        // when
        Page<Bookmark> results = bookmarkRepository.findByTitleContainingIgnoreCaseOrUrlContainingIgnoreCase(
                "git", "git", PageRequest.of(0, 10));

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("GitHub");
    }

    @Test
    @DisplayName("북마크를 삭제할 수 있다")
    void deleteBookmark() {
        // given
        Bookmark bookmark = Bookmark.builder()
                .title("Test")
                .url("https://test.com")
                .build();
        Bookmark saved = bookmarkRepository.save(bookmark);

        // when
        bookmarkRepository.deleteById(saved.getId());

        // then
        Optional<Bookmark> found = bookmarkRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}
