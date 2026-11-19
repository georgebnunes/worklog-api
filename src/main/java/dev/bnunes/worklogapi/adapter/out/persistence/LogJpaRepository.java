package dev.bnunes.worklogapi.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface LogJpaRepository extends JpaRepository<LogEntity, Integer> {
}
