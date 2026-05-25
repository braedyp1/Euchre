package com.euchre.platform.entity;

import com.euchre.platform.domain.GamePhase;
import com.euchre.platform.domain.GameStatus;
import com.euchre.platform.domain.Suit;
import com.euchre.platform.domain.Team;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "game_sessions")
public class GameSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus gameStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GamePhase phase;

    @Column(nullable = false)
    private int dealerPosition;

    @Column(nullable = false)
    private int currentTurn;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Suit trumpSuit;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Suit turnedDownSuit;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Team makerTeam;

    @Column
    private Integer makerSeat;

    @Column(nullable = false)
    private boolean loneHand;

    @Column(nullable = false)
    private int scoreTeamA;

    @Column(nullable = false)
    private int scoreTeamB;

    @Column(nullable = false)
    private int tricksTeamA;

    @Column(nullable = false)
    private int tricksTeamB;

    @Column(nullable = false)
    private int currentTrickNumber;

    @Column(nullable = false)
    private int passesThisRound;

    @Column(length = 8)
    private String upcard;

    @Column(length = 20)
    private String winner;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Instant lastActionTimestamp;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        lastActionTimestamp = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getDealerPosition() {
        return dealerPosition;
    }

    public void setDealerPosition(int dealerPosition) {
        this.dealerPosition = dealerPosition;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public Suit getTurnedDownSuit() {
        return turnedDownSuit;
    }

    public void setTurnedDownSuit(Suit turnedDownSuit) {
        this.turnedDownSuit = turnedDownSuit;
    }

    public Team getMakerTeam() {
        return makerTeam;
    }

    public void setMakerTeam(Team makerTeam) {
        this.makerTeam = makerTeam;
    }

    public Integer getMakerSeat() {
        return makerSeat;
    }

    public void setMakerSeat(Integer makerSeat) {
        this.makerSeat = makerSeat;
    }

    public boolean isLoneHand() {
        return loneHand;
    }

    public void setLoneHand(boolean loneHand) {
        this.loneHand = loneHand;
    }

    public int getScoreTeamA() {
        return scoreTeamA;
    }

    public void setScoreTeamA(int scoreTeamA) {
        this.scoreTeamA = scoreTeamA;
    }

    public int getScoreTeamB() {
        return scoreTeamB;
    }

    public void setScoreTeamB(int scoreTeamB) {
        this.scoreTeamB = scoreTeamB;
    }

    public int getTricksTeamA() {
        return tricksTeamA;
    }

    public void setTricksTeamA(int tricksTeamA) {
        this.tricksTeamA = tricksTeamA;
    }

    public int getTricksTeamB() {
        return tricksTeamB;
    }

    public void setTricksTeamB(int tricksTeamB) {
        this.tricksTeamB = tricksTeamB;
    }

    public int getCurrentTrickNumber() {
        return currentTrickNumber;
    }

    public void setCurrentTrickNumber(int currentTrickNumber) {
        this.currentTrickNumber = currentTrickNumber;
    }

    public int getPassesThisRound() {
        return passesThisRound;
    }

    public void setPassesThisRound(int passesThisRound) {
        this.passesThisRound = passesThisRound;
    }

    public String getUpcard() {
        return upcard;
    }

    public void setUpcard(String upcard) {
        this.upcard = upcard;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Instant getLastActionTimestamp() {
        return lastActionTimestamp;
    }

    public void touchActionTimestamp() {
        lastActionTimestamp = Instant.now();
    }
}
