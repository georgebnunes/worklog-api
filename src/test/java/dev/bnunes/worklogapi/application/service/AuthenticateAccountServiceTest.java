package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase.AuthenticationResult;
import dev.bnunes.worklogapi.application.port.out.AccessTokenIssuer;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.application.port.out.RefreshTokenGenerator;
import dev.bnunes.worklogapi.application.port.out.RefreshTokenRepository;
import dev.bnunes.worklogapi.application.port.out.TokenHasher;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthenticateAccountServiceTest {

    private final AccountRepository accounts = mock(AccountRepository.class);
    private final AccessTokenIssuer accessTokenIssuer = mock(AccessTokenIssuer.class);
    private final RefreshTokenGenerator refreshTokenGenerator = mock(RefreshTokenGenerator.class);
    private final TokenHasher tokenHasher = mock(TokenHasher.class);
    private final RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
    private final AuthenticateAccountService service = new AuthenticateAccountService(
            accounts, accessTokenIssuer, refreshTokenGenerator, tokenHasher, refreshTokens);

    @Test
    void answersWithAJwtShapedTokenThatExpiresInThirtyMinutesAndARefreshToken() {
        Account frank = new Account(1, "frank", "frank@frank.com");
        given(accounts.findByEmail("frank@frank.com")).willReturn(Optional.of(frank));
        given(accessTokenIssuer.issue(frank, Duration.ofMinutes(30))).willReturn("a.b.c");
        given(refreshTokenGenerator.generate()).willReturn("a-refresh-token");

        AuthenticationResult result = service.authenticate("frank@frank.com", "password123");

        assertThat(result.token()).matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");
        assertThat(result.expiresIn()).isEqualTo(1800);
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void answersWithTheGeneratedRefreshTokenAndStoresOnlyItsHashForSevenDays() {
        Account ivy = new Account(7, "ivy", "ivy@ivy.com");
        given(accounts.findByEmail("ivy@ivy.com")).willReturn(Optional.of(ivy));
        given(accessTokenIssuer.issue(ivy, Duration.ofMinutes(30))).willReturn("a.b.c");
        given(refreshTokenGenerator.generate()).willReturn("raw-refresh-token");
        given(tokenHasher.hash("raw-refresh-token")).willReturn("hashed-refresh-token");

        Instant before = Instant.now();
        AuthenticationResult result = service.authenticate("ivy@ivy.com", "password123");
        Instant after = Instant.now();

        assertThat(result.refreshToken()).isEqualTo("raw-refresh-token");
        ArgumentCaptor<Instant> expiresAt = ArgumentCaptor.forClass(Instant.class);
        verify(refreshTokens).save(eq(7L), eq("hashed-refresh-token"), expiresAt.capture());
        assertThat(expiresAt.getValue()).isBetween(before.plus(7, ChronoUnit.DAYS), after.plus(7, ChronoUnit.DAYS));
    }

    @Test
    void rejectsAnEmailThatNoAccountHas() {
        given(accounts.findByEmail("nobody@nobody.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("nobody@nobody.com", "password123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
