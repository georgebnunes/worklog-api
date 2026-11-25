package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.domain.model.Log;

import java.util.List;

/** The list answer: the logs sit under {@code data.items}. */
record LogListResponse(Data data) {

    record Data(List<LogResponse> items) {
    }

    static LogListResponse from(List<Log> logs) {
        return new LogListResponse(new Data(logs.stream().map(LogResponse::from).toList()));
    }
}
