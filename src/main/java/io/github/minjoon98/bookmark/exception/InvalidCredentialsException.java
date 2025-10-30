package io.github.minjoon98.bookmark.exception;

import org.springframework.http.HttpStatus;

import io.github.minjoon98.bookmark.global.exception.BookmarkException;

import static io.github.minjoon98.bookmark.exception.AuthExceptionConstant.INVALID_CREDENTIALS;

public class InvalidCredentialsException extends BookmarkException {

    private static final AuthExceptionConstant constant = INVALID_CREDENTIALS;

    public InvalidCredentialsException() {
        super(constant.getMessage());
    }

    @Override
    public HttpStatus getStatus() {
        return constant.getHttpStatus();
    }
}
