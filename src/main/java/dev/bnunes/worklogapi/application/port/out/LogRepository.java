package dev.bnunes.worklogapi.application.port.out;

import dev.bnunes.worklogapi.domain.model.Log;

public interface LogRepository {

    Log save(long userId, String content);
}
