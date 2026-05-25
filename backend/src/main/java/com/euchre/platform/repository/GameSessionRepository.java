package com.euchre.platform.repository;

import com.euchre.platform.domain.GameStatus;
import com.euchre.platform.entity.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSessionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GameSessionEntity> findWithLockById(Long id);

    Optional<GameSessionEntity> findFirstByPlayerIdAndGameStatusOrderByUpdatedAtDesc(Long playerId, GameStatus gameStatus);
}
