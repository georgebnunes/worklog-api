package dev.bnunes.worklogapi.adapter.out.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import dev.bnunes.worklogapi.domain.model.Account;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenIssuerTest {

    private static final String SECRET = "unit-test-secret-with-at-least-32-bytes";

    private final Account account = new Account(42, "frank", "frank@frank.com");

    @Test
    void issuesAnHs256JwtSignedWithTheSecretThatNamesTheAccountAndExpiresAfterTheLifetime() throws Exception {
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        SignedJWT jwt = SignedJWT.parse(new JwtAccessTokenIssuer(SECRET).issue(account, Duration.ofMinutes(30)));

        Instant after = Instant.now();
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
        assertThat(jwt.verify(new MACVerifier(SECRET.getBytes(StandardCharsets.UTF_8)))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("42");
        assertThat(jwt.getJWTClaimsSet().getIssueTime().toInstant()).isBetween(before, after);
        assertThat(jwt.getJWTClaimsSet().getExpirationTime().toInstant())
                .isEqualTo(jwt.getJWTClaimsSet().getIssueTime().toInstant().plus(Duration.ofMinutes(30)));
    }

    @Test
    void refusesToSignWithASecretShorterThan32Bytes() {
        JwtAccessTokenIssuer issuer = new JwtAccessTokenIssuer("too-short");

        assertThatThrownBy(() -> issuer.issue(account, Duration.ofMinutes(30)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not sign the access token");
    }
}
