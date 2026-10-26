package dev.bnunes.worklogapi.application.port.out;

import java.time.Instant;

public interface RefreshTokenRepository {

    void save(long accountId, String tokenHash, Instant expiresAt);
}
