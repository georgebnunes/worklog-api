package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.CreateAccountUseCase;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.stereotype.Service;

@Service
class CreateAccountService implements CreateAccountUseCase {

    @Override
    public Account create(String username, String password, String email) {
        return new Account(1L, username, email);
    }
}
