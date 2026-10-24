package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase;
import dev.bnunes.worklogapi.application.port.out.AccessTokenIssuer;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
class AuthenticateAccountService implements AuthenticateAccountUseCase {

    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
    // Placeholder until the refresh token rule drives real issuance.
    private static final String REFRESH_TOKEN_PLACEHOLDER = "pending-refresh-token";

    private final AccountRepository accounts;
    private final AccessTokenIssuer accessTokenIssuer;

    AuthenticateAccountService(AccountRepository accounts, AccessTokenIssuer accessTokenIssuer) {
        this.accounts = accounts;
        this.accessTokenIssuer = accessTokenIssuer;
    }

    @Override
    public AuthenticationResult authenticate(String email, String password) {
        Account account = accounts.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        String token = accessTokenIssuer.issue(account, TOKEN_LIFETIME);
        return new AuthenticationResult(token, (int) TOKEN_LIFETIME.toSeconds(), REFRESH_TOKEN_PLACEHOLDER);
    }
}
