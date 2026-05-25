package com.euchre.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "hand_states", uniqueConstraints = @UniqueConstraint(columnNames = {"game_session_id", "seat_position"}))
public class HandStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    @Column(nullable = false)
    private int seatPosition;

    @Column(nullable = false, columnDefinition = "text")
    private String serializedCards;

    public GameSessionEntity getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSessionEntity gameSession) {
        this.gameSession = gameSession;
    }

    public int getSeatPosition() {
        return seatPosition;
    }

    public void setSeatPosition(int seatPosition) {
        this.seatPosition = seatPosition;
    }

    public String getSerializedCards() {
        return serializedCards;
    }

    public void setSerializedCards(String serializedCards) {
        this.serializedCards = serializedCards;
    }
}
