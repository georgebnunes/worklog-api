package dev.bnunes.worklogapi.acceptance;

import dev.bnunes.worklogapi.acceptance.support.TestAuthServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JwtAuthenticationAcceptanceTests.AuthoritiesProbeController.class)
class JwtAuthenticationAcceptanceTests {

    private static final String PROTECTED_PATH = "/worklog-api/v1/logs";
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

    @Autowired
    JwtAuthenticationAcceptanceTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void requestWithoutAuthorizationHeaderIsRejectedWithPlain401() throws Exception {
        String body = mockMvc.perform(get(PROTECTED_PATH))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("session expired");
    }

    @Test
    void requestWithValidAccessTokenIsAuthenticated() throws Exception {
        int httpStatus = mockMvc.perform(get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_SERVER.accessToken()))
                .andReturn().getResponse().getStatus();

        // No controller exists yet, so an authenticated request ends in 404. It becomes 201/200 with the logs story.
        assertThat(httpStatus).isNotEqualTo(401);
    }

    @Test
    void requestWithExpiredTokenIsRejectedWithSessionExpiredBody() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_SERVER.expiredToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\":\"session expired\"}", JsonCompareMode.STRICT));
    }

    @Test
    void refreshTokenIsNotAcceptedAsAccessToken() throws Exception {
        String body = mockMvc.perform(get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_SERVER.refreshToken()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("session expired");
    }

    @Test
    void tokenFromAnotherIssuerIsRejectedWithPlain401() throws Exception {
        String token = AUTH_SERVER.accessTokenWith(claims -> claims.issuer("another-auth-service"));

        String body = mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("session expired");
    }

    @Test
    void tokenForAnotherAudienceIsRejectedWithPlain401() throws Exception {
        String token = AUTH_SERVER.accessTokenWith(claims -> claims.audience("another-api"));

        String body = mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("session expired");
    }

    @Test
    void rolesClaimBecomesRoleAuthorities() throws Exception {
        mockMvc.perform(get("/test-probe/authorities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_SERVER.accessToken()))
                .andExpect(status().isOk())
                // Spring Security 7 adds FACTOR_BEARER to every login done with a bearer token; ROLE_USER comes from
                // `roles`. The authorities are a set, so their order is not stable: LENIENT ignores order but still
                // requires exactly these two elements.
                .andExpect(content().json("[\"FACTOR_BEARER\",\"ROLE_USER\"]", JsonCompareMode.LENIENT));
    }

    @Test
    void requestWithTamperedTokenIsRejectedWithPlain401() throws Exception {
        String body = mockMvc.perform(get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_SERVER.tamperedToken()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("session expired");
    }

    /** Test-only endpoint: there is no real controller yet, so this exposes what the security layer resolved. */
    @RestController
    static class AuthoritiesProbeController {

        @GetMapping("/test-probe/authorities")
        List<String> authorities(Authentication authentication) {
            return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        }
    }
}
