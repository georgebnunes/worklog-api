package dev.bnunes.worklogapi.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogTests {

    @Test
    void newLogHasOwnerAndContentAndLeavesTheValuesAssignedOnInsertEmpty() {
        Log log = Log.newLog("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet");

        assertThat(log).isEqualTo(new Log(null, "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11",
                "lorem ipsum dolor sit amet", null, null, null));
    }
}
