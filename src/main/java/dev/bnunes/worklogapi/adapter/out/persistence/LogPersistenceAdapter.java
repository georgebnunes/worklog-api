package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.springframework.stereotype.Component;

@Component
class LogPersistenceAdapter implements LogRepository {

    private final LogJpaRepository logJpaRepository;

    LogPersistenceAdapter(LogJpaRepository logJpaRepository) {
        this.logJpaRepository = logJpaRepository;
    }

    @Override
    public Log save(Log log) {
        // Flush so the values the database assigns on insert (active, createdAt, updatedAt) are read back.
        return logJpaRepository.saveAndFlush(new LogEntity(log.userUUID(), log.content())).toDomain();
    }
}
