package dev.bnunes.worklogapi.application.port.out;

import dev.bnunes.worklogapi.domain.model.Account;

import java.time.Duration;

public interface AccessTokenIssuer {

    String issue(Account account, Duration lifetime);
}
