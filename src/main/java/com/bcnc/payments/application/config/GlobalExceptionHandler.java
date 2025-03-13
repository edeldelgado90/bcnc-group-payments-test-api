package com.bcnc.payments.application.config;

import com.bcnc.payments.domain.error.ErrorResponse;
import com.bcnc.payments.domain.price.PriceNotFoundException;
import com.bcnc.payments.domain.price.PriceOverlappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<ErrorResponse> getResponseEntity(
            HttpStatus status, String message) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        LocalDateTime.now(), status.value(), message, UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceNotFoundException(PriceNotFoundException ex) {
        return getResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PriceOverlappingException.class)
    public ResponseEntity<ErrorResponse> handlePriceOverlappingException(
            PriceOverlappingException ex) {
        return getResponseEntity(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
