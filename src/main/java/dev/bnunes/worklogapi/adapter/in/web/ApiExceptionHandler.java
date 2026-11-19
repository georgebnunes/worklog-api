package dev.bnunes.worklogapi.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse invalidRequest(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }
}
