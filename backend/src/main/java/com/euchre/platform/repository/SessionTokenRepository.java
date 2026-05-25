package com.euchre.platform.repository;

import com.euchre.platform.entity.SessionTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionTokenEntity, Long> {
    Optional<SessionTokenEntity> findByToken(String token);
}
