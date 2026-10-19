package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.domain.exception.UsernameAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AccountExceptionHandler {

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    void usernameAlreadyTaken() {
    }
}
