package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.RegisterLogUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/worklog-api/v1/logs")
class LogController {

    private final RegisterLogUseCase registerLog;

    LogController(RegisterLogUseCase registerLog) {
        this.registerLog = registerLog;
    }

    /** The principal's name is the JWT {@code sub} claim, the account that owns the log. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LogResponse register(@RequestBody RegisterLogRequest request, Principal principal) {
        return LogResponse.from(registerLog.register(principal.getName(), request.content()));
    }
}
