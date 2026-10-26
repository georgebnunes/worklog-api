package dev.bnunes.worklogapi.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256TokenHasherTest {

    private final Sha256TokenHasher hasher = new Sha256TokenHasher();

    @Test
    void hashesWithSha256AsLowercaseHex() {
        assertThat(hasher.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void hashesTheSameTokenToTheSameValue() {
        assertThat(hasher.hash("some-token")).isEqualTo(hasher.hash("some-token"));
    }
}
