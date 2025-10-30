package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.dto.request.LoginRequest;
import io.github.minjoon98.bookmark.dto.request.SignUpRequest;
import io.github.minjoon98.bookmark.dto.response.LoginResponse;

public interface AuthService {

    void signUp(SignUpRequest request);

    LoginResponse login(LoginRequest request);
}
