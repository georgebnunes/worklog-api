package dev.bnunes.worklogapi.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataAccountRepository extends JpaRepository<AccountEntity, Integer> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<AccountEntity> findByEmailIgnoreCase(String email);
}
