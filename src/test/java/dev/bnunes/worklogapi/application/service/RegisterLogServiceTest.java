package dev.bnunes.worklogapi.application.service;

import dev.bnunes.worklogapi.application.port.out.AccessTokenVerifier;
import dev.bnunes.worklogapi.application.port.out.LogRepository;
import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RegisterLogServiceTest {

    private final AccessTokenVerifier accessTokenVerifier = mock(AccessTokenVerifier.class);
    private final LogRepository logs = mock(LogRepository.class);
    private final RegisterLogService service = new RegisterLogService(accessTokenVerifier, logs);

    @Test
    void savesTheContentForTheAccountNamedByTheAccessToken() {
        Instant now = Instant.parse("2026-10-21T21:44:44Z");
        Log saved = new Log(5, "lorem ipsum dolor sit amet", 7, true, now, now);
        given(accessTokenVerifier.verify("a.b.c")).willReturn(7L);
        given(logs.save(7, "lorem ipsum dolor sit amet")).willReturn(saved);

        Log result = service.register("a.b.c", "lorem ipsum dolor sit amet");

        assertThat(result).isEqualTo(saved);
    }
}
