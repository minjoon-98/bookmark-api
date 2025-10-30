package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.entity.Bookmark;
import io.github.minjoon98.bookmark.entity.User;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .password("password")
                .build());
    }

    @Test
    @DisplayName("북마크를 저장하고 조회할 수 있다")
    void saveAndFindBookmark() {
        // given
        Bookmark bookmark = Bookmark.builder()
                .title("Google")
                .url("https://www.google.com")
                .memo("검색 엔진")
                .user(testUser)
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
                .user(testUser)
                .build();
        Bookmark saved = bookmarkRepository.save(bookmark);

        // when
        Optional<Bookmark> found = bookmarkRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("GitHub");
    }

    @Test
    @DisplayName("사용자별로 제목이나 URL로 북마크를 검색할 수 있다 (대소문자 무시)")
    void searchByTitleOrUrl() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .title("Google")
                .url("https://www.google.com")
                .user(testUser)
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .title("GitHub")
                .url("https://github.com")
                .user(testUser)
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .title("Stack Overflow")
                .url("https://stackoverflow.com")
                .user(testUser)
                .build());

        // when
        Page<Bookmark> results = bookmarkRepository.findByUserAndTitleContainingIgnoreCaseOrUserAndUrlContainingIgnoreCase(
                testUser, "git", testUser, "git", PageRequest.of(0, 10));

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
                .user(testUser)
                .build();
        Bookmark saved = bookmarkRepository.save(bookmark);

        // when
        bookmarkRepository.deleteById(saved.getId());

        // then
        Optional<Bookmark> found = bookmarkRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}
