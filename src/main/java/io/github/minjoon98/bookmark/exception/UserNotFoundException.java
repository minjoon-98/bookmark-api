package io.github.minjoon98.bookmark.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("사용자를 찾을 수 없습니다. ID: " + id);
    }

    public UserNotFoundException(String email) {
        super("사용자를 찾을 수 없습니다. Email: " + email);
    }
}
