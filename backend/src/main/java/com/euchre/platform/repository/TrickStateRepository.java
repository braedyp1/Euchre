package com.euchre.platform.repository;

import com.euchre.platform.entity.TrickStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrickStateRepository extends JpaRepository<TrickStateEntity, Long> {
    List<TrickStateEntity> findByGameSessionIdOrderByTrickNumber(Long gameSessionId);

    Optional<TrickStateEntity> findByGameSessionIdAndTrickNumber(Long gameSessionId, int trickNumber);

    void deleteByGameSessionId(Long gameSessionId);
}
