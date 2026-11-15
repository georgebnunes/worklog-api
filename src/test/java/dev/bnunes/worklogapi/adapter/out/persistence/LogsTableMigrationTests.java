package dev.bnunes.worklogapi.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Checks the schema Flyway produces, since the logs entity does not exist yet for ddl-auto=validate to check. */
@SpringBootTest
class LogsTableMigrationTests {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    LogsTableMigrationTests(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void logsOwnerIsTheJwtSubjectStoredInUserUuidColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("""
                SELECT column_name, data_type, character_maximum_length, is_nullable
                FROM information_schema.columns
                WHERE table_name = 'logs'
                """);

        assertThat(columns).extracting(column -> column.get("column_name"))
                .contains("userUUID")
                .doesNotContain("userId");

        Map<String, Object> userUuid = columns.stream()
                .filter(column -> "userUUID".equals(column.get("column_name")))
                .findFirst().orElseThrow();
        assertThat(userUuid)
                .containsEntry("data_type", "character varying")
                .containsEntry("character_maximum_length", 180)
                .containsEntry("is_nullable", "NO");
    }
}
