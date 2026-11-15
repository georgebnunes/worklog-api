package dev.bnunes.worklogapi.adapter.in.web.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Answers every authentication failure with 401, keeping the standard Bearer {@code WWW-Authenticate} header. Only a
 * token that was valid but is past its expiry gets the {@code session expired} body, so a client tries its refresh
 * token only when that can work. A missing, malformed, tampered or otherwise invalid token never gets that body.
 */
@Component
class SessionExpiredAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String SESSION_EXPIRED_BODY = "{\"message\":\"session expired\"}";

    // Spring's JwtTimestampValidator reports an expired token with a description starting like this.
    private static final String EXPIRED_DESCRIPTION_PREFIX = "Jwt expired at";

    private final AuthenticationEntryPoint bearerTokenEntryPoint = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        bearerTokenEntryPoint.commence(request, response, authException);

        if (isExpiredToken(authException)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(SESSION_EXPIRED_BODY);
        }
    }

    private static boolean isExpiredToken(AuthenticationException authException) {
        return authException.getCause() instanceof JwtValidationException validationFailure
                && validationFailure.getErrors().stream()
                .anyMatch(error -> error.getDescription() != null
                        && error.getDescription().startsWith(EXPIRED_DESCRIPTION_PREFIX));
    }
}
