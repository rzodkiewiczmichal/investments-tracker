package com.investments.tracker.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<ValidationError> details,
        String path,
        String traceId) {
    public static ErrorResponse badRequest(
            String message, List<ValidationError> details, String path, String traceId) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC),
                400,
                "Bad Request",
                message,
                details,
                path,
                traceId);
    }

    public static ErrorResponse notFound(String message, String path, String traceId) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC), 404, "Not Found", message, null, path, traceId);
    }

    public static ErrorResponse conflict(String message, String path, String traceId) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC), 409, "Conflict", message, null, path, traceId);
    }

    public static ErrorResponse unprocessableEntity(String message, String path, String traceId) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC),
                422,
                "Unprocessable Entity",
                message,
                null,
                path,
                traceId);
    }

    public static ErrorResponse internalError(String path, String traceId) {
        return new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC),
                500,
                "Internal Server Error",
                "An unexpected error occurred",
                null,
                path,
                traceId);
    }
}
