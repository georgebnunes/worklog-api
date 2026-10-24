package dev.bnunes.worklogapi.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashesWithBCryptAndNeverReturnsThePassword() {
        assertThat(hasher.hash("password123"))
                .isNotEqualTo("password123")
                .matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    @Test
    void saltsSoTheSamePasswordHashesDifferentlyEachTime() {
        assertThat(hasher.hash("password123")).isNotEqualTo(hasher.hash("password123"));
    }
}
