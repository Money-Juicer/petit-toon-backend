package com.petit.toon.controller.exception;

import com.petit.toon.controller.exception.response.ErrorResponse;
import com.petit.toon.exception.PetitToonException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(PetitToonException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(PetitToonException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(ErrorResponse.builder()
                        .code(String.valueOf(e.getStatusCode()))
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValid(MethodArgumentNotValidException e) {
        ErrorResponse body = ErrorResponse.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .message("잘못된 요청입니다.")
                .build();
        e.getFieldErrors().forEach(fieldError ->
                body.addValidation(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message("요청 데이터를 처리할 수 없습니다.")
                        .build());
    }
}
