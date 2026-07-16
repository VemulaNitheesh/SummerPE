package com.medicinecommerce.inventoryservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) { return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of()); }
    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ApiError> handleProductNotFound(ProductNotFoundException ex) { return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of()); }
    @ExceptionHandler(ProductServiceUnavailableException.class)
    ResponseEntity<ApiError> handleProductServiceUnavailable(ProductServiceUnavailableException ex) { return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), Map.of()); }
    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiError> handleConflict(DuplicateResourceException ex) { return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of()); }
    @ExceptionHandler(InsufficientInventoryException.class)
    ResponseEntity<ApiError> handleInsufficientInventory(InsufficientInventoryException ex) { return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream().collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(), (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception ex) { return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", Map.of()); }
    private ResponseEntity<ApiError> build(HttpStatus status, String message, Map<String, String> errors) { return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, errors)); }
}
