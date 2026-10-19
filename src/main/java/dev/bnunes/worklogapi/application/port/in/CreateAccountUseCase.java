package dev.bnunes.worklogapi.application.port.in;

import dev.bnunes.worklogapi.domain.model.Account;

public interface CreateAccountUseCase {

    Account create(String username, String password, String email);
}
