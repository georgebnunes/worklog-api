package dev.bnunes.worklogapi.application.port.out;

public interface AccessTokenVerifier {

    long verify(String token);
}
