package io.github.minjoon98.bookmark.global.exception;

import org.springframework.http.HttpStatus;

public abstract class BookmarkException extends RuntimeException {

    protected BookmarkException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();
}
