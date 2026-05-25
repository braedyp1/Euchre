package com.euchre.platform.entity;

import com.euchre.platform.domain.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "action_logs")
public class ActionLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    @Column(nullable = false, length = 40)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActionType actionType;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false)
    private Instant timestamp;

    @PrePersist
    void prePersist() {
        timestamp = Instant.now();
    }

    public void setGameSession(GameSessionEntity gameSession) {
        this.gameSession = gameSession;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
