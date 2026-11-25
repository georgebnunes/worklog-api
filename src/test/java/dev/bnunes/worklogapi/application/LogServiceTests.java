package dev.bnunes.worklogapi.application;

import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogServiceTests {

    private static final Instant NOW = Instant.parse("2026-10-21T21:44:44Z");

    private final RecordingLogRepository repository = new RecordingLogRepository();
    private final LogService service = new LogService(repository);

    @Test
    void registersANewLogForTheOwnerAndReturnsTheSavedOne() {
        Log registered = service.register("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet");

        assertThat(repository.received).containsExactly(
                Log.newLog("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet"));
        assertThat(registered).isEqualTo(new Log(1, "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11",
                "lorem ipsum dolor sit amet", true, NOW, NOW));
    }

    @Test
    void listsTheActiveLogsOfTheAccountAskedFor() {
        Log agenda = new Log(15, "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "I'll work on the weeks agenda",
                true, NOW, NOW);
        repository.activeLogs = List.of(agenda);

        List<Log> listed = service.listActive("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11");

        assertThat(repository.askedFor).containsExactly("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11");
        assertThat(listed).containsExactly(agenda);
    }

    /** In-memory stand-in for the out-port: it records what the service sends and answers like the database would. */
    private static final class RecordingLogRepository implements LogRepository {

        private final List<Log> received = new ArrayList<>();
        private final List<String> askedFor = new ArrayList<>();
        private List<Log> activeLogs = List.of();

        @Override
        public Log save(Log log) {
            received.add(log);
            return new Log(1, log.userUUID(), log.content(), true, NOW, NOW);
        }

        @Override
        public List<Log> findActiveByUser(String userUUID) {
            askedFor.add(userUUID);
            return activeLogs;
        }
    }
}
