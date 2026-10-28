package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.domain.model.Log;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LogPersistenceAdapter.class, AccountPersistenceAdapter.class})
class LogPersistenceAdapterTest {

    @Autowired
    private LogPersistenceAdapter adapter;

    @Autowired
    private AccountPersistenceAdapter accounts;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesTheLogForTheUserAndAnswersWithTheServerGeneratedValues() {
        long userId = accounts.save("logadapter1", "bcrypt-hash", "logadapter1@adapter.com").id();

        Log log = adapter.save(userId, "lorem ipsum dolor sit amet");
        entityManager.flush();

        assertThat(log.content()).isEqualTo("lorem ipsum dolor sit amet");
        assertThat(log.userId()).isEqualTo(userId);
        assertThat(log.active()).isTrue();
        assertThat(log.createdAt()).isNotNull().isEqualTo(log.updatedAt());
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT id, \"userId\", content, active FROM logs WHERE id = ?", (int) log.id());
        assertThat(row.get("userId")).isEqualTo((int) userId);
        assertThat(row.get("content")).isEqualTo("lorem ipsum dolor sit amet");
        assertThat(row.get("active")).isEqualTo(true);
    }
}
