package com.investments.tracker.infrastructure.web.exception;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.investments.tracker.application.exception.ResourceAlreadyExistsException;
import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.exception.InvalidPriceException;
import com.investments.tracker.domain.exception.InvalidQuantityException;
import com.investments.tracker.domain.exception.InvalidSymbolException;
import com.investments.tracker.infrastructure.web.dto.ErrorResponse;
import com.investments.tracker.infrastructure.web.dto.ValidationError;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                e ->
                                        new ValidationError(
                                                e.getField(),
                                                e.getDefaultMessage(),
                                                e.getRejectedValue()))
                        .toList();
        return ErrorResponse.badRequest(
                "Validation failed", errors, request.getRequestURI(), getTraceId());
    }

    @ExceptionHandler({
        InvalidQuantityException.class,
        InvalidPriceException.class,
        InvalidSymbolException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainValidation(DomainException ex, HttpServletRequest request) {
        return ErrorResponse.badRequest(
                ex.getMessage(), null, request.getRequestURI(), getTraceId());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ErrorResponse.notFound(ex.getMessage(), request.getRequestURI(), getTraceId());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return ErrorResponse.conflict(ex.getMessage(), request.getRequestURI(), getTraceId());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        return ErrorResponse.internalError(request.getRequestURI(), getTraceId());
    }

    private String getTraceId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().traceId() : "unknown";
    }
}
