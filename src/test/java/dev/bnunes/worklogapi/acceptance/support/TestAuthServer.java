package dev.bnunes.worklogapi.acceptance.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Stands in for bn-auth-service: serves a JWKS at /.well-known/jwks.json and signs tokens shaped like the ones
 * bn-auth-service issues (iss, aud, sub, jti, type, roles), so tests exercise the real jwk-set-uri path.
 */
public final class TestAuthServer {

    public static final String ISSUER = "bn-auth-service";
    public static final String AUDIENCE = "bn-api";

    private final RSAKey signingKey;
    private final HttpServer server;

    private TestAuthServer(RSAKey signingKey, HttpServer server) {
        this.signingKey = signingKey;
        this.server = server;
    }

    public static TestAuthServer start() {
        try {
            RSAKey key = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
            byte[] jwks = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);

            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/.well-known/jwks.json", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jwks.length);
                exchange.getResponseBody().write(jwks);
                exchange.close();
            });
            server.start();
            return new TestAuthServer(key, server);
        } catch (JOSEException | IOException e) {
            throw new IllegalStateException("Could not start the stub auth server", e);
        }
    }

    public void stop() {
        server.stop(0);
    }

    public String jwksUri() {
        return "http://127.0.0.1:%d/.well-known/jwks.json".formatted(server.getAddress().getPort());
    }

    public String accessToken() {
        return sign(claims("access", Instant.now(), Instant.now().plus(15, ChronoUnit.MINUTES)));
    }

    /** bn-auth-service signs refresh tokens with the same key, issuer and audience as access tokens. */
    public String refreshToken() {
        return sign(claims("refresh", Instant.now(), Instant.now().plus(7, ChronoUnit.DAYS)));
    }

    /** Correctly signed, but already past its expiry (beyond the decoder's 60s clock skew). */
    public String expiredToken() {
        Instant expiredAt = Instant.now().minus(10, ChronoUnit.MINUTES);
        return sign(claims("access", expiredAt.minus(15, ChronoUnit.MINUTES), expiredAt));
    }

    /** A correctly signed token whose payload was swapped afterwards, so the signature no longer matches. */
    public String tamperedToken() {
        String[] parts = accessToken().split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                claims("access", Instant.now(), Instant.now().plus(15, ChronoUnit.MINUTES)).toString().getBytes(StandardCharsets.UTF_8));
        return parts[0] + "." + forgedPayload + "." + parts[2];
    }

    /** A valid access token except for the claims the caller overrides, e.g. {@code b -> b.issuer("someone-else")}. */
    public String accessTokenWith(Consumer<JWTClaimsSet.Builder> overrides) {
        JWTClaimsSet.Builder builder = claimsBuilder("access", Instant.now(), Instant.now().plus(15, ChronoUnit.MINUTES));
        overrides.accept(builder);
        return sign(builder.build());
    }

    private static JWTClaimsSet claims(String type, Instant issuedAt, Instant expiresAt) {
        return claimsBuilder(type, issuedAt, expiresAt).build();
    }

    private static JWTClaimsSet.Builder claimsBuilder(String type, Instant issuedAt, Instant expiresAt) {
        return new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .subject(UUID.randomUUID().toString())
                .jwtID(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("roles", List.of("USER"))
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt));
    }

    private String sign(JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(signingKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign the test token", e);
        }
    }
}
