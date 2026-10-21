package dev.bnunes.worklogapi.application.port.out;

import dev.bnunes.worklogapi.domain.model.Account;

public interface AccountRepository {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Account save(String username, String password, String email);
}
