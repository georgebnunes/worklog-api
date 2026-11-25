package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.LogUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** The principal's name is the JWT {@code sub} claim, the account that owns the logs. */
@RestController
@RequestMapping("/worklog-api/v1/logs")
class LogController {

    private final LogUseCase logUseCase;

    LogController(LogUseCase logUseCase) {
        this.logUseCase = logUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LogResponse register(@Valid @RequestBody RegisterLogRequest request, Principal principal) {
        return LogResponse.from(logUseCase.register(principal.getName(), request.content()));
    }

    @GetMapping
    LogListResponse list(Principal principal) {
        return LogListResponse.from(logUseCase.listActive(principal.getName()));
    }
}
