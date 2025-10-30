package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 저장할 수 있다")
    void save() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 사용자를 찾을 수 있다")
    void findByEmail() {
        // given
        User user = User.builder()
                .email("user@example.com")
                .password("password123")
                .build();
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("user@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user@example.com");
        assertThat(found.get().getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
    void findByEmail_notFound() {
        // when
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        User user = User.builder()
                .email("exists@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // when & then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    @DisplayName("동일한 이메일의 사용자는 중복 저장할 수 없다")
    void unique_constraint_email() {
        // given
        User user1 = User.builder()
                .email("duplicate@example.com")
                .password("password1")
                .build();
        userRepository.save(user1);
        userRepository.flush();

        // when & then - 같은 이메일로 저장 시도
        User user2 = User.builder()
                .email("duplicate@example.com")
                .password("password2")
                .build();
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("여러 사용자를 저장하고 조회할 수 있다")
    void save_and_find_multiple_users() {
        // given
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password1")
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .password("password2")
                .build();
        User user3 = User.builder()
                .email("user3@example.com")
                .password("password3")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // when
        long count = userRepository.count();

        // then
        assertThat(count).isEqualTo(3);
        assertThat(userRepository.findByEmail("user1@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user2@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user3@example.com")).isPresent();
    }

    @Test
    @DisplayName("사용자를 삭제할 수 있다")
    void delete() {
        // given
        User user = User.builder()
                .email("todelete@example.com")
                .password("password")
                .build();
        User saved = userRepository.save(user);

        // when
        userRepository.delete(saved);

        // then
        assertThat(userRepository.findById(saved.getId())).isEmpty();
        assertThat(userRepository.findByEmail("todelete@example.com")).isEmpty();
    }

    @Test
    @DisplayName("이메일은 대소문자를 구분한다")
    void email_case_sensitive() {
        // given
        User user = User.builder()
                .email("Test@Example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // when & then
        assertThat(userRepository.findByEmail("Test@Example.com")).isPresent();
        assertThat(userRepository.findByEmail("test@example.com")).isEmpty();
        assertThat(userRepository.findByEmail("TEST@EXAMPLE.COM")).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID로 조회할 수 있다")
    void findById() {
        // given
        User user = User.builder()
                .email("findbyid@example.com")
                .password("password")
                .build();
        User saved = userRepository.save(user);

        // when
        Optional<User> found = userRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findbyid@example.com");
    }

    @Test
    @DisplayName("비밀번호는 null이 될 수 없다")
    void password_not_null() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password(null)
                .build();

        // when & then
        assertThatThrownBy(() -> {
            userRepository.save(user);
            userRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("이메일은 null이 될 수 없다")
    void email_not_null() {
        // given
        User user = User.builder()
                .email(null)
                .password("password")
                .build();

        // when & then
        assertThatThrownBy(() -> {
            userRepository.save(user);
            userRepository.flush();
        }).isInstanceOf(Exception.class);
    }
}
