package dev.bnunes.worklogapi.application.port.out;

import dev.bnunes.worklogapi.domain.model.Log;

import java.util.List;

public interface LogRepository {

    /** Persists the log and returns it with the values assigned on insert. */
    Log save(Log log);

    /** The active logs owned by the account. */
    List<Log> findActiveByUser(String userUUID);
}
