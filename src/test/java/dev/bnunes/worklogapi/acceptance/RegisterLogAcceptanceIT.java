package dev.bnunes.worklogapi.acceptance;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Register a work log")
class RegisterLogAcceptanceIT {

    private static final String ACCOUNTS_PATH = "/worklog-api/v1/accounts";
    private static final String AUTH_PATH = "/worklog-api/v1/auth";
    private static final String LOGS_PATH = "/worklog-api/v1/logs";
    private static final String UTC_TIMESTAMP_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$";

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Must create a log from `content` for the account in the JWT, and answer 201 with `id`, `content`, `userId`, `active`, `createdAt` and `updatedAt`.")
    class CreateLog {

        @Test
        @DisplayName("The one where an authenticated account sends valid content. The log is saved and the response is 201 with `userId` equal to the JWT subject and `active` true.")
        void authenticatedAccountSendsValidContent() throws Exception {
            int accountId = register("logger1", "password123", "logger1@logger.com");
            String token = login("logger1@logger.com", "password123");

            mockMvc.perform(post(LOGS_PATH)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content":"lorem ipsum dolor sit amet"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.content").value("lorem ipsum dolor sit amet"))
                    .andExpect(jsonPath("$.userId").value(accountId))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.createdAt").value(matchesPattern(UTC_TIMESTAMP_PATTERN)))
                    .andExpect(jsonPath("$.updatedAt").value(matchesPattern(UTC_TIMESTAMP_PATTERN)));
        }

        @Test
        @DisplayName("The one where the body also sends `userId`, `active`, `createdAt` or `updatedAt`. They are ignored and the stored values come from the JWT and the server.")
        void clientSentOwnerStateAndTimestampsAreIgnored() throws Exception {
            int accountId = register("logger2", "password123", "logger2@logger.com");
            String token = login("logger2@logger.com", "password123");

            mockMvc.perform(post(LOGS_PATH)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content":"lorem ipsum dolor sit amet","userId":%d,"active":false,
                                     "createdAt":"2020-01-01T00:00:00Z","updatedAt":"2020-01-01T00:00:00Z"}
                                    """.formatted(accountId + 1000)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(accountId))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.createdAt").value(not("2020-01-01T00:00:00Z")))
                    .andExpect(jsonPath("$.updatedAt").value(not("2020-01-01T00:00:00Z")));
        }
    }

    private int register(String username, String password, String email) throws Exception {
        String body = mockMvc.perform(post(ACCOUNTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s","email":"%s"}
                                """.formatted(username, password, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post(AUTH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
