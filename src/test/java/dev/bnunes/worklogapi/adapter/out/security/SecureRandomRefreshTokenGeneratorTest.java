package dev.bnunes.worklogapi.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomRefreshTokenGeneratorTest {

    private final SecureRandomRefreshTokenGenerator generator = new SecureRandomRefreshTokenGenerator();

    @Test
    void generatesAUrlSafeTokenFrom32RandomBytes() {
        assertThat(generator.generate()).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void generatesADifferentTokenEachTime() {
        assertThat(generator.generate()).isNotEqualTo(generator.generate());
    }
}
