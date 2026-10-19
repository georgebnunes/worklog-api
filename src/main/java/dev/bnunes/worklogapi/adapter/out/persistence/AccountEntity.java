package dev.bnunes.worklogapi.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounts_id_seq")
    @SequenceGenerator(name = "accounts_id_seq", sequenceName = "accounts_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    protected AccountEntity() {
    }

    AccountEntity(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    Integer getId() {
        return id;
    }

    String getUsername() {
        return username;
    }

    String getEmail() {
        return email;
    }
}
