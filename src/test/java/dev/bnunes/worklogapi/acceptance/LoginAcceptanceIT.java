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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "JWT_SECRET=" + LoginAcceptanceIT.JWT_SECRET)
@AutoConfigureMockMvc
@DisplayName("Login with JWT")
class LoginAcceptanceIT {

    static final String JWT_SECRET = "acceptance-test-secret-with-at-least-32-bytes";

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

    @Nested
    @DisplayName("Must issue a JWT signed with HS256 that identifies the account and expires 30 minutes after it is issued.")
    class IssueJwt {

        @Test
        @DisplayName("The one where login succeeds at 10:00. The JWT carries the account id as its subject and expires at 10:30.")
        void jwtCarriesAccountIdAndExpiresInThirtyMinutes() throws Exception {
            String accountId = register("login3", "password123", "hank@hank.com");

            Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            String token = login("hank@hank.com", "password123");
            Instant after = Instant.now();

            String[] parts = token.split("\\.");
            String header = decode(parts[0]);
            String payload = decode(parts[1]);

            assertThat(JsonPath.<String>read(header, "$.alg")).isEqualTo("HS256");
            assertThat(parts[2]).isEqualTo(hs256Signature(parts[0] + "." + parts[1]));
            assertThat(JsonPath.<String>read(payload, "$.sub")).isEqualTo(accountId);
            assertThat(JsonPath.<Number>read(payload, "$.exp").longValue())
                    .isBetween(before.plusSeconds(1800).getEpochSecond(), after.plusSeconds(1800).getEpochSecond());
        }
    }

    private String register(String username, String password, String email) throws Exception {
        String body = mockMvc.perform(post(ACCOUNTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s","email":"%s"}
                                """.formatted(username, password, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return String.valueOf(JsonPath.<Object>read(body, "$.id"));
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

    private static String decode(String base64Url) {
        return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
    }

    private static String hs256Signature(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}
