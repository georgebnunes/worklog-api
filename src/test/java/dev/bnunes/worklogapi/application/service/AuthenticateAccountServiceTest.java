package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase.AuthenticationResult;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthenticateAccountServiceTest {

    private final AccountRepository accounts = mock(AccountRepository.class);
    private final AuthenticateAccountService service = new AuthenticateAccountService(accounts);

    @Test
    void answersWithAJwtShapedTokenThatExpiresInThirtyMinutesAndARefreshToken() {
        given(accounts.findByEmail("frank@frank.com")).willReturn(Optional.of(new Account(1, "frank", "frank@frank.com")));

        AuthenticationResult result = service.authenticate("frank@frank.com", "password123");

        assertThat(result.token()).matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");
        assertThat(result.expiresIn()).isEqualTo(1800);
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void rejectsAnEmailThatNoAccountHas() {
        given(accounts.findByEmail("nobody@nobody.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("nobody@nobody.com", "password123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
