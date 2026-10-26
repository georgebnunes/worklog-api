package dev.bnunes.worklogapi.application.port.out;

public interface TokenHasher {

    String hash(String token);
}
