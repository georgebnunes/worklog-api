package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.CreateAccountUseCase;
import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.domain.exception.EmailAlreadyRegisteredException;
import dev.bnunes.worklogapi.domain.exception.UsernameAlreadyTakenException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.stereotype.Service;

@Service
class CreateAccountService implements CreateAccountUseCase {

    // Placeholder until the "never store the password in plain text" rule drives real hashing.
    private static final String PASSWORD_HASH_PLACEHOLDER = "{pending-hash}";

    private final AccountRepository accounts;

    CreateAccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public Account create(String username, String password, String email) {
        if (accounts.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
        }
        if (accounts.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        return accounts.save(username, PASSWORD_HASH_PLACEHOLDER, email);
    }
}
