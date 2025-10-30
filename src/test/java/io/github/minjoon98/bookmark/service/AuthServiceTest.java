package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.util.IssueTokenResolver;
import io.github.minjoon98.bookmark.dto.request.LoginRequest;
import io.github.minjoon98.bookmark.dto.request.SignUpRequest;
import io.github.minjoon98.bookmark.dto.response.LoginResponse;
import io.github.minjoon98.bookmark.entity.User;
import io.github.minjoon98.bookmark.exception.DuplicateEmailException;
import io.github.minjoon98.bookmark.exception.InvalidCredentialsException;
import io.github.minjoon98.bookmark.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IssueTokenResolver issueTokenResolver;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testUser, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpRequest request = new SignUpRequest("new@example.com", "password123");
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        authService.signUp(request);

        // then
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicate_email() {
        // given
        SignUpRequest request = new SignUpRequest("test@example.com", "password123");
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(issueTokenResolver.issueToken(testUser)).willReturn("jwt-token-here");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("jwt-token-here");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(issueTokenResolver, times(1)).issueToken(testUser);
    }

    @Test
    @DisplayName("로그인 실패 - 사용자를 찾을 수 없음")
    void login_fail_user_not_found() {
        // given
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");
        given(userRepository.findByEmail("nonexistent@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(issueTokenResolver, never()).issueToken(any(User.class));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_invalid_password() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
        verify(issueTokenResolver, never()).issueToken(any(User.class));
    }

    @Test
    @DisplayName("빈 이메일로 회원가입 시도")
    void signUp_with_empty_email() {
        // given
        SignUpRequest request = new SignUpRequest("", "password123");
        given(userRepository.existsByEmail("")).willReturn(false);

        // when
        authService.signUp(request);

        // then
        verify(userRepository, times(1)).existsByEmail("");
    }

    @Test
    @DisplayName("이메일 대소문자 구분 없이 로그인 가능")
    void login_case_insensitive_email() {
        // given
        LoginRequest request = new LoginRequest("TEST@EXAMPLE.COM", "password123");
        given(userRepository.findByEmail("TEST@EXAMPLE.COM")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(issueTokenResolver.issueToken(testUser)).willReturn("jwt-token-here");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("jwt-token-here");
        verify(userRepository, times(1)).findByEmail("TEST@EXAMPLE.COM");
    }
}
