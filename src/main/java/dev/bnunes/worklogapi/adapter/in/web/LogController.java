package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.RegisterLogUseCase;
import dev.bnunes.worklogapi.domain.model.Log;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/worklog-api/v1/logs")
class LogController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RegisterLogUseCase registerLog;

    LogController(RegisterLogUseCase registerLog) {
        this.registerLog = registerLog;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LogResponse register(@RequestHeader("Authorization") String authorization, @RequestBody RegisterLogRequest request) {
        Log log = registerLog.register(authorization.substring(BEARER_PREFIX.length()), request.content());
        return new LogResponse(log.id(), log.content(), log.userId(), log.active(), log.createdAt(), log.updatedAt());
    }

    record RegisterLogRequest(String content) {
    }

    record LogResponse(long id, String content, long userId, boolean active, Instant createdAt, Instant updatedAt) {
    }
}
