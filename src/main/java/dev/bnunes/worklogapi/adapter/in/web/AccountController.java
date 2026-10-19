package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.CreateAccountUseCase;
import dev.bnunes.worklogapi.domain.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worklog-api/v1/accounts")
class AccountController {

    private final CreateAccountUseCase createAccount;

    AccountController(CreateAccountUseCase createAccount) {
        this.createAccount = createAccount;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccountResponse create(@RequestBody CreateAccountRequest request) {
        Account account = createAccount.create(request.username(), request.password(), request.email());
        return new AccountResponse(account.id(), account.username(), account.email());
    }

    record CreateAccountRequest(String username, String password, String email) {
    }

    record AccountResponse(long id, String username, String email) {
    }
}
