package io.github.minjoon98.bookmark.exception;

import org.springframework.http.HttpStatus;

import io.github.minjoon98.bookmark.global.exception.BookmarkException;

import static io.github.minjoon98.bookmark.exception.AuthExceptionConstant.DUPLICATE_EMAIL;

public class DuplicateEmailException extends BookmarkException {

    private static final AuthExceptionConstant constant = DUPLICATE_EMAIL;

    public DuplicateEmailException(String email) {
        super(constant.getMessage() + " (" + email + ")");
    }

    @Override
    public HttpStatus getStatus() {
        return constant.getHttpStatus();
    }
}
