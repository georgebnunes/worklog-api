package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

// NONE keeps the Testcontainers Postgres from application.properties instead of an embedded database.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(LogPersistenceAdapter.class)
class LogPersistenceAdapterTests {

    private final LogPersistenceAdapter adapter;

    @Autowired
    LogPersistenceAdapterTests(LogPersistenceAdapter adapter) {
        this.adapter = adapter;
    }

    @Test
    void savedLogComesBackWithTheValuesTheDatabaseAssigned() {
        Log saved = adapter.save(Log.newLog("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet"));

        assertThat(saved.id()).isPositive();
        assertThat(saved.userUUID()).isEqualTo("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11");
        assertThat(saved.content()).isEqualTo("lorem ipsum dolor sit amet");
        assertThat(saved.active()).isTrue();
        assertThat(saved.createdAt()).isNotNull().isEqualTo(saved.updatedAt());
    }
}
