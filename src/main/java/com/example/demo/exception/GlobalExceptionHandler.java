package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    log.warn("NOT_FOUND: {}", ex.getMessage());
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toViolation).toList();

    log.warn("VALIDATION_FAILED: violations={}", violations.size());
    return build(HttpStatus.BAD_REQUEST, "Validation failed", req, violations);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
    log.warn("BAD_JSON: {}", ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, "Malformed JSON or wrong field types", req, null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
    log.warn("DATA_INTEGRITY: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
    return build(HttpStatus.CONFLICT, "Data conflict / constraint violation", req, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("UNEXPECTED_ERROR", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, null);
  }

  private ApiError.FieldViolation toViolation(FieldError fe) {
    return ApiError.FieldViolation.builder()
        .field(fe.getField())
        .message(fe.getDefaultMessage())
        .build();
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req,
                                        List<ApiError.FieldViolation> violations) {

    ApiError body = ApiError.builder()
        .timestamp(OffsetDateTime.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(message)
        .path(req.getRequestURI())
        .traceId(MDC.get("traceId"))
        .violations(violations)
        .build();

    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
  }
}
