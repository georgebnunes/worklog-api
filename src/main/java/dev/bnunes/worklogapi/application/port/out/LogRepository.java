package dev.bnunes.worklogapi.application.port.out;

import dev.bnunes.worklogapi.domain.model.Log;

public interface LogRepository {

    /** Persists the log and returns it with the values assigned on insert. */
    Log save(Log log);
}
