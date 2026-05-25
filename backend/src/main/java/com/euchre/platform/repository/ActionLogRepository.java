package com.euchre.platform.repository;

import com.euchre.platform.entity.ActionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLogEntity, Long> {
}
