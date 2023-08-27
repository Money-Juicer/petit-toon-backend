package com.petit.toon.exception.notfound;

import com.petit.toon.exception.PetitToonException;
import org.springframework.http.HttpStatus;

public class BookmarkNotFoundException extends PetitToonException {
    public static final String MESSAGE = "존재하지 않거나 이미 컬렉션에서 삭제된 항목입니다.";

    public BookmarkNotFoundException() {
        super(MESSAGE);
    }

    public BookmarkNotFoundException(Throwable cause) {
        super(MESSAGE, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.NOT_FOUND.value();
    }
}
