package com.euchre.platform.repository;

import com.euchre.platform.entity.HandStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HandStateRepository extends JpaRepository<HandStateEntity, Long> {
    List<HandStateEntity> findByGameSessionIdOrderBySeatPosition(Long gameSessionId);

    Optional<HandStateEntity> findByGameSessionIdAndSeatPosition(Long gameSessionId, int seatPosition);

    void deleteByGameSessionId(Long gameSessionId);
}
