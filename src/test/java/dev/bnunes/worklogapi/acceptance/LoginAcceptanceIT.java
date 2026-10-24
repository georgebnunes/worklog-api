package dev.bnunes.worklogapi.acceptance;

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
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Login with JWT")
class LoginAcceptanceIT {

    private static final String ACCOUNTS_PATH = "/worklog-api/v1/accounts";
    private static final String AUTH_PATH = "/worklog-api/v1/auth";
    private static final String JWT_PATTERN = "^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$";

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Must authenticate an account from `email` and `password` and answer 200 with `token`, `expiresIn` and `refreshToken`.")
    class AuthenticateAccount {

        @Test
        @DisplayName("The one where the email and password match an active account. The response is 200 with the JWT, `expiresIn` of 1800 seconds, and a refresh token.")
        void emailAndPasswordMatchAnActiveAccount() throws Exception {
            register("login1", "password123", "frank@frank.com");

            mockMvc.perform(post(AUTH_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"frank@frank.com","password":"password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(matchesPattern(JWT_PATTERN)))
                    .andExpect(jsonPath("$.expiresIn").value(1800))
                    .andExpect(jsonPath("$.refreshToken").value(not(emptyString())));
        }

        @Test
        @DisplayName("The one where the email is typed `Grace@Grace.com` and the account was registered as `grace@grace.com`. Login succeeds, because emails are compared ignoring case.")
        void emailTypedWithDifferentCase() throws Exception {
            register("login2", "password123", "grace@grace.com");

            mockMvc.perform(post(AUTH_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"Grace@Grace.com","password":"password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(matchesPattern(JWT_PATTERN)))
                    .andExpect(jsonPath("$.expiresIn").value(1800))
                    .andExpect(jsonPath("$.refreshToken").value(not(emptyString())));
        }
    }

    @Nested
    @DisplayName("Must reject wrong credentials with one identical answer (401), without revealing whether the email exists.")
    class RejectWrongCredentials {

        @Test
        @DisplayName("The one where the email is unknown, whatever the password. The result is 401.")
        void unknownEmail() throws Exception {
            mockMvc.perform(post(AUTH_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"nobody@nobody.com","password":"password123"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    private void register(String username, String password, String email) throws Exception {
        mockMvc.perform(post(ACCOUNTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s","email":"%s"}
                                """.formatted(username, password, email)))
                .andExpect(status().isCreated());
    }
}
