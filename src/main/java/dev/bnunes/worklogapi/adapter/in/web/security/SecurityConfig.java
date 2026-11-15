package dev.bnunes.worklogapi.adapter.in.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.web.SecurityFilterChain;

/**
 * worklog-api is a stateless resource server: it never issues tokens, it only verifies the JWTs signed by
 * bn-auth-service, using the public keys fetched from its JWKS endpoint (spring.security.oauth2.resourceserver.jwt).
 */
@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            SessionExpiredAwareAuthenticationEntryPoint entryPoint) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint))
                .build();
    }

    /**
     * bn-auth-service signs access and refresh tokens with the same key, issuer and audience and tells them apart
     * only by the {@code type} claim. Without this check a refresh token would pass as an access token. Spring Boot
     * adds every {@code OAuth2TokenValidator<Jwt>} bean to the validators of the auto-configured JwtDecoder.
     */
    @Bean
    OAuth2TokenValidator<Jwt> accessTokenTypeValidator() {
        return new JwtClaimValidator<String>("type", "access"::equals);
    }
}
