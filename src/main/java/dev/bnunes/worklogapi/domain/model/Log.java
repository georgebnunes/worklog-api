package dev.bnunes.worklogapi.domain.model;

import java.time.Instant;

public record Log(long id, String content, long userId, boolean active, Instant createdAt, Instant updatedAt) {
}
