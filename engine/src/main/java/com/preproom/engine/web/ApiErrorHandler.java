package com.preproom.engine.web;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Returns our own error messages (for example "The answer is empty") instead of a bare status. */
@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handle(ResponseStatusException e) {
        String reason = e.getReason() == null ? e.getStatusCode().toString() : e.getReason();
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", e.getStatusCode().value(), "message", reason));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(
            org.springframework.http.converter.HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("status", 400, "message", "The request body is not valid JSON in the expected shape"));
    }
}