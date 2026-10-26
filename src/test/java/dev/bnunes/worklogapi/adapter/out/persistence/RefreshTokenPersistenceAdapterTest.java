package dev.bnunes.worklogapi.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RefreshTokenPersistenceAdapter.class, AccountPersistenceAdapter.class})
class RefreshTokenPersistenceAdapterTest {

    @Autowired
    private RefreshTokenPersistenceAdapter adapter;

    @Autowired
    private AccountPersistenceAdapter accounts;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesTheHashAndExpiryOfTheAccount() {
        long accountId = accounts.save("adapter1", "bcrypt-hash", "adapter1@adapter.com").id();
        Instant expiresAt = Instant.parse("2026-11-01T10:00:00Z");

        adapter.save(accountId, "hashed-token", expiresAt);
        entityManager.flush();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT \"accountId\", \"tokenHash\", \"expiresAt\", active FROM refresh_tokens WHERE \"accountId\" = ?",
                (int) accountId);
        assertThat(row.get("tokenHash")).isEqualTo("hashed-token");
        assertThat(((Timestamp) row.get("expiresAt")).toInstant()).isEqualTo(expiresAt);
        assertThat(row.get("active")).isEqualTo(true);
    }
}
