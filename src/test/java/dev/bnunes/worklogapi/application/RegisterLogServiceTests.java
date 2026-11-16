package dev.bnunes.worklogapi.application;

import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterLogServiceTests {

    private static final Instant NOW = Instant.parse("2026-10-21T21:44:44Z");

    @Test
    void registersANewLogForTheOwnerAndReturnsTheSavedOne() {
        List<Log> received = new ArrayList<>();
        RegisterLogService service = new RegisterLogService(log -> {
            received.add(log);
            return new Log(1, log.userUUID(), log.content(), true, NOW, NOW);
        });

        Log registered = service.register("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet");

        assertThat(received).containsExactly(
                Log.newLog("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet"));
        assertThat(registered).isEqualTo(new Log(1, "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11",
                "lorem ipsum dolor sit amet", true, NOW, NOW));
    }
}
