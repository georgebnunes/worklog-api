package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

@Service
class AuthenticateAccountService implements AuthenticateAccountUseCase {

    // Placeholders until the next rules drive account lookup, password check, JWT signing and refresh tokens.
    private static final String TOKEN_PLACEHOLDER = "pending.jwt.signature";
    private static final String REFRESH_TOKEN_PLACEHOLDER = "pending-refresh-token";
    private static final int EXPIRES_IN_SECONDS = 1800;

    private final AccountRepository accounts;

    AuthenticateAccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public AuthenticationResult authenticate(String email, String password) {
        accounts.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        return new AuthenticationResult(TOKEN_PLACEHOLDER, EXPIRES_IN_SECONDS, REFRESH_TOKEN_PLACEHOLDER);
    }
}
