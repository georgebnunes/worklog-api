package dev.bnunes.worklogapi.application.port.in;

public interface AuthenticateAccountUseCase {

    AuthenticationResult authenticate(String email, String password);

    record AuthenticationResult(String token, int expiresIn, String refreshToken) {
    }
}
