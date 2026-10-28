package dev.bnunes.worklogapi.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;

import java.time.Instant;

@Entity
@Table(name = "logs")
class LogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logs_id_seq")
    @SequenceGenerator(name = "logs_id_seq", sequenceName = "logs_id_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String content;

    @Generated
    @Column(name = "createdAt", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated
    @Column(name = "updatedAt", insertable = false, updatable = false)
    private Instant updatedAt;

    @Generated
    @Column(insertable = false, updatable = false)
    private Boolean active;

    protected LogEntity() {
    }

    LogEntity(Integer userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    Integer getId() {
        return id;
    }

    Integer getUserId() {
        return userId;
    }

    String getContent() {
        return content;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    Boolean getActive() {
        return active;
    }
}
