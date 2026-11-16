package dev.bnunes.worklogapi.application;

import dev.bnunes.worklogapi.application.port.in.RegisterLogUseCase;
import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.springframework.stereotype.Service;

@Service
class RegisterLogService implements RegisterLogUseCase {

    private final LogRepository logRepository;

    RegisterLogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public Log register(String userUUID, String content) {
        return logRepository.save(Log.newLog(userUUID, content));
    }
}
