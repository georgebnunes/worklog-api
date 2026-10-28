package dev.bnunes.worklogapi.adapter.out.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import dev.bnunes.worklogapi.application.port.out.AccessTokenVerifier;
import dev.bnunes.worklogapi.domain.exception.InvalidAccessTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

@Component
class JwtAccessTokenVerifier implements AccessTokenVerifier {

    private final byte[] secret;

    JwtAccessTokenVerifier(@Value("${jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public long verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new InvalidAccessTokenException();
            }
            return Long.parseLong(jwt.getJWTClaimsSet().getSubject());
        } catch (ParseException | JOSEException e) {
            throw new InvalidAccessTokenException();
        }
    }
}
