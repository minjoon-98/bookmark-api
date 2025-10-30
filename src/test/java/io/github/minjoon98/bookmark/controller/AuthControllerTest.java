package io.github.minjoon98.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.minjoon98.bookmark.dto.request.LoginRequest;
import io.github.minjoon98.bookmark.dto.request.SignUpRequest;
import io.github.minjoon98.bookmark.dto.response.LoginResponse;
import io.github.minjoon98.bookmark.exception.DuplicateEmailException;
import io.github.minjoon98.bookmark.exception.InvalidCredentialsException;
import io.github.minjoon98.bookmark.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("회원가입 API 테스트 - 성공")
    void signUp_success() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("new@example.com", "password123");
        doNothing().when(authService).signUp(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"));

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 이메일 중복")
    void signUp_duplicate_email() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("duplicate@example.com", "password123");
        doThrow(new DuplicateEmailException("duplicate@example.com"))
                .when(authService).signUp(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 유효성 검증 실패 (빈 이메일)")
    void signUp_invalid_email() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("", "password123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signUp(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 유효성 검증 실패 (빈 비밀번호)")
    void signUp_invalid_password() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("test@example.com", "");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signUp(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 성공")
    void login_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse response = new LoginResponse("jwt-token-here", "test@example.com");
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-here"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 실패 (잘못된 인증 정보)")
    void login_invalid_credentials() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new InvalidCredentialsException());

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 유효성 검증 실패 (빈 이메일)")
    void login_invalid_email() throws Exception {
        // given
        LoginRequest request = new LoginRequest("", "password123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그아웃 API 테스트 - 성공")
    void logout_success() throws Exception {
        // when & then
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다. 클라이언트에서 토큰을 삭제해주세요."));
    }
}
