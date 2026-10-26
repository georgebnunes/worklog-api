package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase;
import dev.bnunes.worklogapi.application.port.out.AccessTokenIssuer;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.application.port.out.RefreshTokenGenerator;
import dev.bnunes.worklogapi.application.port.out.RefreshTokenRepository;
import dev.bnunes.worklogapi.application.port.out.TokenHasher;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
class AuthenticateAccountService implements AuthenticateAccountUseCase {

    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(7);

    private final AccountRepository accounts;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokens;

    AuthenticateAccountService(AccountRepository accounts, AccessTokenIssuer accessTokenIssuer,
                               RefreshTokenGenerator refreshTokenGenerator, TokenHasher tokenHasher,
                               RefreshTokenRepository refreshTokens) {
        this.accounts = accounts;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenHasher = tokenHasher;
        this.refreshTokens = refreshTokens;
    }

    @Override
    public AuthenticationResult authenticate(String email, String password) {
        Account account = accounts.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        String token = accessTokenIssuer.issue(account, TOKEN_LIFETIME);
        return new AuthenticationResult(token, (int) TOKEN_LIFETIME.toSeconds(), issueRefreshToken(account));
    }

    private String issueRefreshToken(Account account) {
        String refreshToken = refreshTokenGenerator.generate();
        refreshTokens.save(account.id(), tokenHasher.hash(refreshToken), Instant.now().plus(REFRESH_TOKEN_LIFETIME));
        return refreshToken;
    }
}
