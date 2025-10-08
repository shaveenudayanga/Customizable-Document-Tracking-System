package com.docutrace.workflow_service.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                      @NonNull HttpHeaders headers,
                                      @NonNull org.springframework.http.HttpStatusCode status,
                                      @NonNull WebRequest request) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        String reason = Optional.ofNullable(HttpStatus.resolve(status.value()))
            .map(HttpStatus::getReasonPhrase)
            .orElse(status.toString());
        ApiError error = ApiError.of(status.value(), reason, "Validation failed", details);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(
                ApiError.of(status.value(), status.getReasonPhrase(), "Constraint violation", details),
                status
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(
                ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), List.of()),
                status
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(
                ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), List.of()),
                status
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = Optional.ofNullable(ex.getMessage()).orElse("Unexpected error");
        return new ResponseEntity<>(
                ApiError.of(status.value(), status.getReasonPhrase(), message, List.of()),
                status
        );
    }
}
