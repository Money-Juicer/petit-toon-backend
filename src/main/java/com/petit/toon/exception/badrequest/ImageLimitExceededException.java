package com.petit.toon.exception.badrequest;

import com.petit.toon.exception.PetitToonException;
import org.springframework.http.HttpStatus;

public class ImageLimitExceededException extends PetitToonException {
    public static final String MESSAGE = "이미지의 개수는 10개를 초과할 수 없습니다.";

    public ImageLimitExceededException() {
        super(MESSAGE);
    }

    public ImageLimitExceededException(Throwable cause) {
        super(MESSAGE, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
