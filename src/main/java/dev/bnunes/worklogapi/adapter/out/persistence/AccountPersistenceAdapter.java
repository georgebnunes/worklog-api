package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.application.port.out.AccountRepository;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
class AccountPersistenceAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;

    AccountPersistenceAdapter(SpringDataAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Account save(String username, String password, String email) {
        AccountEntity saved = repository.save(new AccountEntity(username, password, email));
        return new Account(saved.getId(), saved.getUsername(), saved.getEmail());
    }
}
