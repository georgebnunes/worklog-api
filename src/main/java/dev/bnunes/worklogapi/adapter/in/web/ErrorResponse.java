package dev.bnunes.worklogapi.adapter.in.web;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/** Body of an error answer: {@code message} tells the client what to fix, {@code path} is the request it refers to. */
record ErrorResponse(Instant timestamp, Integer status, String error, String message, String path) {

    static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
