package dev.bnunes.worklogapi.acceptance;

import com.jayway.jsonpath.JsonPath;
import dev.bnunes.worklogapi.acceptance.support.TestAuthServer;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Register a work log")
class RegisterLogAcceptanceIT {

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
    RegisterLogAcceptanceIT(MockMvc mockMvc, JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nested
    @DisplayName("Must create a log from `content` for the account in the JWT, and answer 201 with `id`, `content`, "
            + "`active`, `createdAt` and `updatedAt`.")
    class CreateLogForTheAccountInTheJwt {

        @Test
        @DisplayName("The one where an authenticated account sends valid content. The log is saved with `userUUID` "
                + "equal to the JWT subject and the response is 201 with `active` true. The response does not "
                + "return `userUUID`.")
        void authenticatedAccountSendsValidContent() throws Exception {
            String subject = UUID.randomUUID().toString();

            String body = mockMvc.perform(post(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"lorem ipsum dolor sit amet\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            assertThat(JsonPath.<Map<String, Object>>read(body, "$").keySet())
                    .containsExactlyInAnyOrder("id", "content", "active", "createdAt", "updatedAt");
            Integer id = JsonPath.read(body, "$.id");
            assertThat(id).isPositive();
            assertThat(JsonPath.<String>read(body, "$.content")).isEqualTo("lorem ipsum dolor sit amet");
            assertThat(JsonPath.<Boolean>read(body, "$.active")).isTrue();
            assertUtcInstantNearNow(JsonPath.read(body, "$.createdAt"));
            assertUtcInstantNearNow(JsonPath.read(body, "$.updatedAt"));

            List<Map<String, Object>> saved = jdbcTemplate.queryForList(
                    "SELECT id, content, \"userUUID\" FROM logs WHERE \"userUUID\" = ?", subject);
            assertThat(saved).hasSize(1);
            assertThat(saved.getFirst())
                    .containsEntry("id", id)
                    .containsEntry("content", "lorem ipsum dolor sit amet")
                    .containsEntry("userUUID", subject);
        }

        @Test
        @DisplayName("The one where the body also sends `userId`, `userUUID`, `active`, `createdAt` or `updatedAt`. "
                + "They are ignored: the owner comes from the JWT, `active` is the database default (`true`) and the "
                + "timestamps come from the server.")
        void bodyAlsoSendsOwnerActiveAndTimestamps() throws Exception {
            String subject = UUID.randomUUID().toString();
            String anotherAccount = UUID.randomUUID().toString();

            String body = mockMvc.perform(post(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content":"lorem ipsum dolor sit amet",
                                     "userId":999,
                                     "userUUID":"%s",
                                     "active":false,
                                     "createdAt":"2020-01-01T00:00:00Z",
                                     "updatedAt":"2020-01-01T00:00:00Z"}
                                    """.formatted(anotherAccount)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            // The client sent active=false: true here is the column default, not a value taken from the body.
            assertThat(JsonPath.<Boolean>read(body, "$.active")).isTrue();
            assertUtcInstantNearNow(JsonPath.read(body, "$.createdAt"));
            assertUtcInstantNearNow(JsonPath.read(body, "$.updatedAt"));

            assertThat(jdbcTemplate.queryForList(
                    "SELECT 1 FROM logs WHERE \"userUUID\" = ?", anotherAccount)).isEmpty();
            Map<String, Object> saved = jdbcTemplate.queryForMap(
                    "SELECT \"userUUID\", active, \"createdAt\" FROM logs WHERE \"userUUID\" = ?", subject);
            assertThat(saved).containsEntry("userUUID", subject).containsEntry("active", true);
        }
    }

    @Nested
    @DisplayName("Must trim `content`, then reject it when missing, empty, or longer than 140 characters (400).")
    class ValidateContent {

        @Test
        @DisplayName("The one where `content` is missing. The answer is 400 and nothing is saved.")
        void contentIsMissing() throws Exception {
            String subject = UUID.randomUUID().toString();

            mockMvc.perform(post(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            assertThat(jdbcTemplate.queryForList(
                    "SELECT 1 FROM logs WHERE \"userUUID\" = ?", subject)).isEmpty();
        }

        @Test
        @DisplayName("The one where `content` is only spaces (`\"   \"`). The answer is 400 and nothing is saved.")
        void contentIsOnlySpaces() throws Exception {
            String subject = UUID.randomUUID().toString();

            mockMvc.perform(post(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"   \"}"))
                    .andExpect(status().isBadRequest());

            assertThat(jdbcTemplate.queryForList(
                    "SELECT 1 FROM logs WHERE \"userUUID\" = ?", subject)).isEmpty();
        }

        @Test
        @DisplayName("The one where `content` has 141 characters. The answer is 400 with the message "
                + "`Max characters allowed 140.` and nothing is saved.")
        void contentHas141Characters() throws Exception {
            String subject = UUID.randomUUID().toString();

            String body = mockMvc.perform(post(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"" + "a".repeat(141) + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();

            assertThat(JsonPath.<Map<String, Object>>read(body, "$").keySet())
                    .containsExactlyInAnyOrder("timestamp", "status", "error", "message", "path");
            assertThat(JsonPath.<Integer>read(body, "$.status")).isEqualTo(400);
            assertThat(JsonPath.<String>read(body, "$.error")).isEqualTo("Bad Request");
            assertThat(JsonPath.<String>read(body, "$.message")).isEqualTo("Max characters allowed 140.");
            assertThat(JsonPath.<String>read(body, "$.path")).isEqualTo(LOGS_PATH);
            assertUtcInstantNearNow(JsonPath.read(body, "$.timestamp"));

            assertThat(jdbcTemplate.queryForList(
                    "SELECT 1 FROM logs WHERE \"userUUID\" = ?", subject)).isEmpty();
        }
    }

    private static String tokenFor(String subject) {
        return AUTH_SERVER.accessTokenWith(claims -> claims.subject(subject));
    }

    /** The server clock, in UTC with a trailing Z; a client-supplied "last week" would fail the window. */
    private static void assertUtcInstantNearNow(String timestamp) {
        assertThat(timestamp).endsWith("Z");
        assertThat(Instant.parse(timestamp))
                .isBetween(Instant.now().minus(1, ChronoUnit.MINUTES), Instant.now().plus(1, ChronoUnit.MINUTES));
    }
}
