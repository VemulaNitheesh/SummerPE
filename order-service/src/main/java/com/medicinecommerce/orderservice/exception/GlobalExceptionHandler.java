package com.medicinecommerce.orderservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ResourceNotFoundException.class, ProductNotFoundException.class})
    ResponseEntity<ApiError> handleNotFound(RuntimeException ex) { return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of()); }
    @ExceptionHandler(InsufficientInventoryException.class)
    ResponseEntity<ApiError> handleConflict(InsufficientInventoryException ex) { return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of()); }
    @ExceptionHandler(DownstreamServiceUnavailableException.class)
    ResponseEntity<ApiError> handleUnavailable(DownstreamServiceUnavailableException ex) { return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), Map.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(), (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception ex) { return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", Map.of()); }
    private ResponseEntity<ApiError> build(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, errors));
    }
}
