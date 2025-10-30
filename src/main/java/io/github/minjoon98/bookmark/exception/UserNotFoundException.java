package io.github.minjoon98.bookmark.exception;

import org.springframework.http.HttpStatus;

import io.github.minjoon98.bookmark.global.exception.BookmarkException;

import static io.github.minjoon98.bookmark.exception.AuthExceptionConstant.USER_NOT_FOUND;

public class UserNotFoundException extends BookmarkException {

    private static final AuthExceptionConstant constant = USER_NOT_FOUND;

    public UserNotFoundException(Long id) {
        super(constant.getMessage() + " (ID: " + id + ")");
    }

    public UserNotFoundException(String email) {
        super(constant.getMessage() + " (Email: " + email + ")");
    }

    @Override
    public HttpStatus getStatus() {
        return constant.getHttpStatus();
    }
}
