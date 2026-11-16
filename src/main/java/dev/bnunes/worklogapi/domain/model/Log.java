package dev.bnunes.worklogapi.domain.model;

import java.time.Instant;

/**
 * A work log owned by an account, identified by the JWT subject. {@code id}, {@code active}, {@code createdAt} and
 * {@code updatedAt} are assigned when the log is persisted, so they are null on a log that was not saved yet.
 */
public record Log(Integer id, String userUUID, String content, Boolean active, Instant createdAt, Instant updatedAt) {

    public static Log newLog(String userUUID, String content) {
        return new Log(null, userUUID, content, null, null, null);
    }
}
