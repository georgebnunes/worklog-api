package dev.bnunes.worklogapi.application.port.in;

import dev.bnunes.worklogapi.domain.model.Log;

import java.util.List;

/** What an account can do with its work logs. */
public interface LogUseCase {

    Log register(String userUUID, String content);

    /** The active logs of the account, never the logs of another account. */
    List<Log> listActive(String userUUID);
}
