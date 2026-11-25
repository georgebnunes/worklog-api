package dev.bnunes.worklogapi.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface LogJpaRepository extends JpaRepository<LogEntity, Integer> {

    List<LogEntity> findByUserUUIDAndActiveTrue(String userUUID);
}
