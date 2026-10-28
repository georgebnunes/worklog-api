package dev.bnunes.worklogapi.domain.exception;

public class InvalidAccessTokenException extends RuntimeException {

    public InvalidAccessTokenException() {
        super("Invalid access token");
    }
}
