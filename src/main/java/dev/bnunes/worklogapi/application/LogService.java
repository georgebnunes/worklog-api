package dev.bnunes.worklogapi.application;

import dev.bnunes.worklogapi.application.port.in.LogUseCase;
import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class LogService implements LogUseCase {

    private final LogRepository logRepository;

    LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public Log register(String userUUID, String content) {
        return logRepository.save(Log.newLog(userUUID, content));
    }

    @Override
    public List<Log> listActive(String userUUID) {
        return logRepository.findActiveByUser(userUUID);
    }
}
