package dev.bnunes.worklogapi.adapter.out.persistence;

import dev.bnunes.worklogapi.domain.model.Log;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Entity
@Table(name = "logs")
class LogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logs_id_seq")
    @SequenceGenerator(name = "logs_id_seq", sequenceName = "logs_id_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "userUUID", nullable = false, updatable = false)
    private String userUUID;

    @Column(nullable = false)
    private String content;

    // The three columns below are left out of the INSERT so the database defaults apply, then read back.
    @Generated(event = EventType.INSERT)
    @Column(insertable = false, updatable = false)
    private Boolean active;

    @Generated(event = EventType.INSERT)
    @Column(name = "createdAt", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "updatedAt", insertable = false, updatable = false)
    private Instant updatedAt;

    protected LogEntity() {
    }

    LogEntity(String userUUID, String content) {
        this.userUUID = userUUID;
        this.content = content;
    }

    Log toDomain() {
        return new Log(id, userUUID, content, active, createdAt, updatedAt);
    }
}
