package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// NONE keeps the Testcontainers Postgres from application.properties instead of an embedded database.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(LogPersistenceAdapter.class)
class LogQueryPersistenceTests {

    private static final String OWNER = "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11";
    private static final String ANOTHER_ACCOUNT = "9b1d6c52-3e47-4a1f-8c60-0d2e7f5a4b33";

    private final LogPersistenceAdapter adapter;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    LogQueryPersistenceTests(LogPersistenceAdapter adapter, JdbcTemplate jdbcTemplate) {
        this.adapter = adapter;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void findsOnlyTheActiveLogsOfTheAccount() {
        Log visible = adapter.save(Log.newLog(OWNER, "visible log"));
        Log deactivated = adapter.save(Log.newLog(OWNER, "deactivated log"));
        adapter.save(Log.newLog(ANOTHER_ACCOUNT, "log of another account"));
        jdbcTemplate.update("UPDATE logs SET active = FALSE WHERE id = ?", deactivated.id());

        List<Log> found = adapter.findActiveByUser(OWNER);

        assertThat(found).containsExactly(visible);
    }
}
