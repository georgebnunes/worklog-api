package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.CreateAccountUseCase;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.application.port.out.PasswordHasher;
import dev.bnunes.worklogapi.domain.exception.EmailAlreadyRegisteredException;
import dev.bnunes.worklogapi.domain.exception.UsernameAlreadyTakenException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.stereotype.Service;

@Service
class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;

    CreateAccountService(AccountRepository accounts, PasswordHasher passwordHasher) {
        this.accounts = accounts;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Account create(String username, String password, String email) {
        if (accounts.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
        }
        if (accounts.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        return accounts.save(username, passwordHasher.hash(password), email);
    }
}
