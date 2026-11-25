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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("List my work logs")
class ListLogsAcceptanceIT {

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
    ListLogsAcceptanceIT(MockMvc mockMvc, JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nested
    @DisplayName("Must list only the active logs of the account in the JWT, each with `id`, `content`, `active`, "
            + "`createdAt` and `updatedAt`.")
    class ListOnlyTheActiveLogsOfTheAccountInTheJwt {

        @Test
        @DisplayName("The one where an authenticated account with several logs asks for the list. The answer is 200 "
                + "and `data.items` holds its active logs. Each item has exactly those five fields and never "
                + "`userUUID`.")
        void authenticatedAccountWithSeveralLogsAsksForTheList() throws Exception {
            String subject = UUID.randomUUID().toString();
            Integer standup = register(subject, "daily standup");
            Integer review = register(subject, "reviewed the pull requests");
            Integer agenda = register(subject, "I'll work on the weeks agenda");

            String body = mockMvc.perform(get(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Map<String, Object>> items = JsonPath.read(body, "$.data.items");
            assertThat(items).extracting(item -> item.get("id"))
                    .containsExactlyInAnyOrder(standup, review, agenda);
            assertThat(items).extracting(item -> item.get("content"))
                    .containsExactlyInAnyOrder("daily standup", "reviewed the pull requests",
                            "I'll work on the weeks agenda");
            assertThat(items).allSatisfy(item -> {
                assertThat(item.keySet())
                        .containsExactlyInAnyOrder("id", "content", "active", "createdAt", "updatedAt");
                assertThat(item).containsEntry("active", true);
            });
        }

        @Test
        @DisplayName("The one where another account also has logs, or one of the account's own logs was "
                + "deactivated. Neither appears in the list.")
        void logsOfAnotherAccountOrDeactivatedLogsDoNotAppear() throws Exception {
            String subject = UUID.randomUUID().toString();
            String anotherAccount = UUID.randomUUID().toString();
            Integer visible = register(subject, "visible log");
            Integer deactivated = register(subject, "deactivated log");
            register(anotherAccount, "log of another account");
            jdbcTemplate.update("UPDATE logs SET active = FALSE WHERE id = ?", deactivated);

            String body = mockMvc.perform(get(LOGS_PATH)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Map<String, Object>> items = JsonPath.read(body, "$.data.items");
            assertThat(items).hasSize(1);
            assertThat(items).extracting(item -> item.get("id")).containsExactly(visible);
            assertThat(items).extracting(item -> item.get("content")).containsExactly("visible log");
        }
    }

    private Integer register(String subject, String content) throws Exception {
        String body = mockMvc.perform(post(LOGS_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(subject))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static String tokenFor(String subject) {
        return AUTH_SERVER.accessTokenWith(claims -> claims.subject(subject));
    }
}
