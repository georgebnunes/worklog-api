package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.in.RegisterLogUseCase;
import dev.bnunes.worklogapi.application.port.out.AccessTokenVerifier;
import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.springframework.stereotype.Service;

@Service
class RegisterLogService implements RegisterLogUseCase {

    private final AccessTokenVerifier accessTokenVerifier;
    private final LogRepository logs;

    RegisterLogService(AccessTokenVerifier accessTokenVerifier, LogRepository logs) {
        this.accessTokenVerifier = accessTokenVerifier;
        this.logs = logs;
    }

    @Override
    public Log register(String accessToken, String content) {
        long userId = accessTokenVerifier.verify(accessToken);
        return logs.save(userId, content);
    }
}
