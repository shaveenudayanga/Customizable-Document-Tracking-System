// ControllerAdvice: centralizes API error handling and maps exceptions to JSON responses
package com.docutrace.document_service.controller.advice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.docutrace.document_service.controller.error.ApiErrorResponse;
import com.docutrace.document_service.service.exception.DocumentNotFoundException;
import com.docutrace.document_service.service.exception.FileResourceNotFoundException;
import com.docutrace.document_service.service.exception.FileStorageException;
import com.docutrace.document_service.service.exception.QrCodeGenerationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "",
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({DocumentNotFoundException.class, FileResourceNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        log.debug("Resource not found: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({FileStorageException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiErrorResponse> handleFileStorage(RuntimeException ex, HttpServletRequest request) {
        log.warn("File storage error: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiErrorResponse> handleRequestParsing(Exception ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Request is malformed", request.getRequestURI());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(QrCodeGenerationException.class)
    public ResponseEntity<ApiErrorResponse> handleQrGeneration(QrCodeGenerationException ex, HttpServletRequest request) {
        log.error("QR code generation failed", ex);
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate QR code", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
