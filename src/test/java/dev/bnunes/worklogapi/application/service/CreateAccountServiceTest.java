package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.application.port.out.PasswordHasher;
import dev.bnunes.worklogapi.domain.exception.EmailAlreadyRegisteredException;
import dev.bnunes.worklogapi.domain.exception.UsernameAlreadyTakenException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CreateAccountServiceTest {

    private final AccountRepository accounts = mock(AccountRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final CreateAccountService service = new CreateAccountService(accounts, passwordHasher);

    @Test
    void savesTheHashOfThePasswordInsteadOfThePasswordItself() {
        given(passwordHasher.hash("password123")).willReturn("hashed-password");
        given(accounts.save("user1", "hashed-password", "bob@bob.com"))
                .willReturn(new Account(1, "user1", "bob@bob.com"));

        Account created = service.create("user1", "password123", "bob@bob.com");

        assertThat(created).isEqualTo(new Account(1, "user1", "bob@bob.com"));
        verify(accounts, never()).save("user1", "password123", "bob@bob.com");
    }

    @Test
    void rejectsAUsernameThatIsAlreadyTaken() {
        given(accounts.existsByUsername("user1")).willReturn(true);

        assertThatThrownBy(() -> service.create("user1", "password123", "bob@bob.com"))
                .isInstanceOf(UsernameAlreadyTakenException.class);
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegistered() {
        given(accounts.existsByEmail("bob@bob.com")).willReturn(true);

        assertThatThrownBy(() -> service.create("user1", "password123", "bob@bob.com"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
