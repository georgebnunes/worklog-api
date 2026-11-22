package dev.bnunes.worklogapi.acceptance;

import dev.bnunes.worklogapi.acceptance.support.TestAuthServer;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * There is no search endpoint yet (see the spec scope), so logs are registered through the real
 * {@code POST /logs} and searchability is observed with the query the repository will run.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Full-text search on logs")
class FullTextSearchLogsAcceptanceIT {

    private static final String LOGS_PATH = "/worklog-api/v1/logs";
    private static final TestAuthServer AUTH_SERVER = TestAuthServer.start();

    @DynamicPropertySource
    static void authServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", AUTH_SERVER::jwksUri);
    }

    @AfterAll
    static void stopAuthServer() {
        AUTH_SERVER.stop();
    }

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    FullTextSearchLogsAcceptanceIT(MockMvc mockMvc, JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nested
    @DisplayName("Must keep a searchable form of every log's content in sync automatically, including logs that "
            + "existed before.")
    class KeepSearchableFormInSync {

        @Test
        @DisplayName("The one where a log is registered. It can be found by search immediately, with no extra step "
                + "from the application.")
        void registeredLogIsSearchableImmediately() throws Exception {
            String subject = UUID.randomUUID().toString();

            register(subject, "deployed the fixes");

            assertThat(search(subject, "deploy fix")).containsExactly("deployed the fixes");
        }

        @Test
        @DisplayName("The one where logs already existed before the migration ran. They are searchable afterwards.")
        void logsThatExistedBeforeTheMigrationAreSearchableAfterwards() {
            String schema = "preexisting_" + UUID.randomUUID().toString().replace("-", "");
            DataSource dataSource = jdbcTemplate.getDataSource();

            // The schema as it was before the search migration: only V1 and V2.
            Flyway.configure().dataSource(dataSource).schemas(schema).target("2").load().migrate();
            jdbcTemplate.update("INSERT INTO " + schema + ".logs (content, \"userUUID\") VALUES (?, ?)",
                    "deployed the fixes", UUID.randomUUID().toString());

            // Every migration, as the application runs them at start-up.
            Flyway.configure().dataSource(dataSource).schemas(schema).load().migrate();

            List<String> found = jdbcTemplate.queryForList("SELECT content FROM " + schema + ".logs "
                    + "WHERE \"contentSearch\" @@ websearch_to_tsquery('english', ?)", String.class, "deploy fix");
            assertThat(found).containsExactly("deployed the fixes");
        }

        @Test
        @DisplayName("The one where a log's content is changed. Search follows the new content and no longer finds "
                + "the old words.")
        void changedContentIsSearchedByItsNewWords() throws Exception {
            String subject = UUID.randomUUID().toString();
            register(subject, "deployed the fixes");

            jdbcTemplate.update("UPDATE logs SET content = ? WHERE \"userUUID\" = ?",
                    "reviewed the roadmap", subject);

            assertThat(search(subject, "roadmap")).containsExactly("reviewed the roadmap");
            assertThat(search(subject, "deploy")).isEmpty();
        }
    }

    private void register(String subject, String content) throws Exception {
        mockMvc.perform(post(LOGS_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated());
    }

    private List<String> search(String subject, String query) {
        return jdbcTemplate.queryForList("""
                SELECT content FROM logs
                WHERE "userUUID" = ? AND "contentSearch" @@ websearch_to_tsquery('english', ?)
                """, String.class, subject, query);
    }

    private static String tokenFor(String subject) {
        return AUTH_SERVER.accessTokenWith(claims -> claims.subject(subject));
    }
}
