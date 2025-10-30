package io.github.minjoon98.bookmark.exception;

import org.springframework.http.HttpStatus;

import io.github.minjoon98.bookmark.global.exception.BookmarkException;

import static io.github.minjoon98.bookmark.exception.BookmarkExceptionConstant.BOOKMARK_NOT_FOUND;

public class BookmarkNotFoundException extends BookmarkException {

    private static final BookmarkExceptionConstant constant = BOOKMARK_NOT_FOUND;

    public BookmarkNotFoundException(Long id) {
        super(constant.getMessage() + " (ID: " + id + ")");
    }

    @Override
    public HttpStatus getStatus() {
        return constant.getHttpStatus();
    }
}
