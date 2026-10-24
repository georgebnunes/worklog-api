package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase;
import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase.AuthenticationResult;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worklog-api/v1/auth")
class AuthController {

    private final AuthenticateAccountUseCase authenticateAccount;

    AuthController(AuthenticateAccountUseCase authenticateAccount) {
        this.authenticateAccount = authenticateAccount;
    }

    @PostMapping
    LoginResponse login(@RequestBody LoginRequest request) {
        AuthenticationResult result = authenticateAccount.authenticate(request.email(), request.password());
        return new LoginResponse(result.token(), result.expiresIn(), result.refreshToken());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    void invalidCredentials() {
    }

    record LoginRequest(String email, String password) {
    }

    record LoginResponse(String token, int expiresIn, String refreshToken) {
    }
}
