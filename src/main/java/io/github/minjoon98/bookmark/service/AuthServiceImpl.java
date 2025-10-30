package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.dto.request.LoginRequest;
import io.github.minjoon98.bookmark.dto.request.SignUpRequest;
import io.github.minjoon98.bookmark.dto.response.LoginResponse;
import io.github.minjoon98.bookmark.entity.User;
import io.github.minjoon98.bookmark.exception.DuplicateEmailException;
import io.github.minjoon98.bookmark.exception.InvalidCredentialsException;
import io.github.minjoon98.bookmark.repository.UserRepository;
import io.github.minjoon98.bookmark.util.IssueTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IssueTokenResolver issueTokenResolver;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = issueTokenResolver.issueToken(user);
        return new LoginResponse(token, user.getEmail());
    }
}
