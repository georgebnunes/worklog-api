package dev.bnunes.worklogapi.application.port.out;

public interface PasswordHasher {

    String hash(String rawPassword);
}
