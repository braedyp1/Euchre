package com.euchre.platform.service;

import com.euchre.platform.domain.Card;
import com.euchre.platform.domain.PlayedCard;
import com.euchre.platform.domain.Rank;
import com.euchre.platform.domain.Suit;
import com.euchre.platform.domain.Team;
import com.euchre.platform.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class RulesService {
    public List<Card> createDeck() {
        return Arrays.stream(Suit.values())
                .flatMap(suit -> Arrays.stream(Rank.values()).map(rank -> new Card(rank, suit)))
                .toList();
    }

    public Suit effectiveSuit(Card card, Suit trumpSuit) {
        if (isLeftBower(card, trumpSuit)) {
            return trumpSuit;
        }
        return card.suit();
    }

    public boolean isRightBower(Card card, Suit trumpSuit) {
        return trumpSuit != null && card.rank() == Rank.JACK && card.suit() == trumpSuit;
    }

    public boolean isLeftBower(Card card, Suit trumpSuit) {
        return trumpSuit != null
                && card.rank() == Rank.JACK
                && card.suit() != trumpSuit
                && card.suit().isSameColor(trumpSuit);
    }

    public void validateCardPlay(List<Card> hand, List<PlayedCard> currentTrick, Card card, Suit trumpSuit) {
        if (!hand.contains(card)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "That card is not in your hand.");
        }
        if (currentTrick.isEmpty()) {
            return;
        }

        Suit ledSuit = effectiveSuit(currentTrick.getFirst().card(), trumpSuit);
        Suit playedSuit = effectiveSuit(card, trumpSuit);
        boolean hasLedSuit = hand.stream().anyMatch(handCard -> effectiveSuit(handCard, trumpSuit) == ledSuit);
        if (hasLedSuit && playedSuit != ledSuit) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Player must follow suit.");
        }
    }

    public int determineTrickWinner(List<PlayedCard> playedCards, Suit trumpSuit) {
        if (playedCards.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", "Cannot score an empty trick.");
        }
        Suit ledSuit = effectiveSuit(playedCards.getFirst().card(), trumpSuit);
        return playedCards.stream()
                .max(Comparator.comparingInt(playedCard -> cardStrength(playedCard.card(), ledSuit, trumpSuit)))
                .map(PlayedCard::seatPosition)
                .orElseThrow();
    }

    public int makerScore(Team makerTeam, int tricksTeamA, int tricksTeamB, boolean loneHand) {
        int makerTricks = makerTeam == Team.TEAM_A ? tricksTeamA : tricksTeamB;
        if (makerTricks < 3) {
            return 0;
        }
        if (makerTricks == 5) {
            return loneHand ? 4 : 2;
        }
        return 1;
    }

    public int defenderScore(Team makerTeam, int tricksTeamA, int tricksTeamB) {
        int makerTricks = makerTeam == Team.TEAM_A ? tricksTeamA : tricksTeamB;
        return makerTricks < 3 ? 2 : 0;
    }

    public int cardStrength(Card card, Suit ledSuit, Suit trumpSuit) {
        if (isRightBower(card, trumpSuit)) {
            return 200;
        }
        if (isLeftBower(card, trumpSuit)) {
            return 190;
        }
        Suit effectiveSuit = effectiveSuit(card, trumpSuit);
        int rankValue = switch (card.rank()) {
            case ACE -> 60;
            case KING -> 50;
            case QUEEN -> 40;
            case JACK -> 30;
            case TEN -> 20;
            case NINE -> 10;
        };
        if (effectiveSuit == trumpSuit) {
            return 100 + rankValue;
        }
        if (effectiveSuit == ledSuit) {
            return rankValue;
        }
        return 0;
    }
}
