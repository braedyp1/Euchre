package com.euchre.platform.entity;

import com.euchre.platform.domain.Suit;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "trick_states", uniqueConstraints = @UniqueConstraint(columnNames = {"game_session_id", "trick_number"}))
public class TrickStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Suit leadSuit;

    @Column
    private Integer winningSeat;

    @Column(nullable = false, columnDefinition = "text")
    private String playedCards;

    @Column(nullable = false)
    private int trickNumber;

    public GameSessionEntity getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSessionEntity gameSession) {
        this.gameSession = gameSession;
    }

    public Suit getLeadSuit() {
        return leadSuit;
    }

    public void setLeadSuit(Suit leadSuit) {
        this.leadSuit = leadSuit;
    }

    public Integer getWinningSeat() {
        return winningSeat;
    }

    public void setWinningSeat(Integer winningSeat) {
        this.winningSeat = winningSeat;
    }

    public String getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(String playedCards) {
        this.playedCards = playedCards;
    }

    public int getTrickNumber() {
        return trickNumber;
    }

    public void setTrickNumber(int trickNumber) {
        this.trickNumber = trickNumber;
    }
}
