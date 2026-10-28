package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.springframework.stereotype.Component;

@Component
class LogPersistenceAdapter implements LogRepository {

    private final SpringDataLogRepository repository;

    LogPersistenceAdapter(SpringDataLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public Log save(long userId, String content) {
        LogEntity saved = repository.saveAndFlush(new LogEntity(Math.toIntExact(userId), content));
        return new Log(saved.getId(), saved.getContent(), saved.getUserId(), saved.getActive(),
                saved.getCreatedAt(), saved.getUpdatedAt());
    }
}
