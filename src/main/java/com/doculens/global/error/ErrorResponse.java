package com.doculens.global.error;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldError> details,
        LocalDateTime timestamp
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse of(int status, String code, String message, List<FieldError> details) {
        return new ErrorResponse(status, code, message, details, LocalDateTime.now());
    }
}
