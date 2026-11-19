package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.domain.model.Log;

import java.time.Instant;

/** The owner is deliberately not part of the response. */
record LogResponse(Integer id, String content, Boolean active, Instant createdAt, Instant updatedAt) {

    static LogResponse from(Log log) {
        return new LogResponse(log.id(), log.content(), log.active(), log.createdAt(), log.updatedAt());
    }
}
