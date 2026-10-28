package dev.bnunes.worklogapi.adapter.out.security;

import dev.bnunes.worklogapi.domain.exception.InvalidAccessTokenException;
import dev.bnunes.worklogapi.domain.model.Account;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenVerifierTest {

    private static final String SECRET = "unit-test-secret-with-at-least-32-bytes";

    private final JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(SECRET);

    @Test
    void answersWithTheAccountIdInTheSubjectOfATokenSignedWithTheSecret() {
        String token = new JwtAccessTokenIssuer(SECRET)
                .issue(new Account(42, "frank", "frank@frank.com"), Duration.ofMinutes(30));

        assertThat(verifier.verify(token)).isEqualTo(42);
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() {
        String token = new JwtAccessTokenIssuer("another-secret-with-at-least-32-bytes")
                .issue(new Account(42, "frank", "frank@frank.com"), Duration.ofMinutes(30));

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(InvalidAccessTokenException.class)
                .hasMessage("Invalid access token");
    }

    @Test
    void rejectsATokenThatIsNotAJwt() {
        assertThatThrownBy(() -> verifier.verify("not-a-jwt"))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    void rejectsAVerifierSecretShorterThan32Bytes() {
        String token = new JwtAccessTokenIssuer(SECRET)
                .issue(new Account(42, "frank", "frank@frank.com"), Duration.ofMinutes(30));

        assertThatThrownBy(() -> new JwtAccessTokenVerifier("too-short").verify(token))
                .isInstanceOf(InvalidAccessTokenException.class);
    }
}
