package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.application.port.out.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repository;

    RefreshTokenPersistenceAdapter(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(long accountId, String tokenHash, Instant expiresAt) {
        repository.save(new RefreshTokenEntity(Math.toIntExact(accountId), tokenHash, expiresAt));
    }
}
