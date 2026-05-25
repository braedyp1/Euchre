package com.euchre.platform.ai;

import com.euchre.platform.domain.Card;
import com.euchre.platform.domain.PlayedCard;
import com.euchre.platform.domain.Rank;
import com.euchre.platform.domain.Suit;
import com.euchre.platform.service.RulesService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AiService {
    private final RulesService rulesService;

    public AiService(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    public boolean shouldOrderUp(List<Card> hand, Suit suit) {
        int score = trumpScore(hand, suit);
        return score >= 72;
    }

    public Suit chooseTrump(List<Card> hand, Suit forbiddenSuit, boolean stickTheDealer) {
        Suit bestSuit = null;
        int bestScore = Integer.MIN_VALUE;
        for (Suit suit : Suit.values()) {
            if (suit == forbiddenSuit) {
                continue;
            }
            int score = trumpScore(hand, suit);
            if (score > bestScore) {
                bestScore = score;
                bestSuit = suit;
            }
        }
        return bestScore >= 70 || stickTheDealer ? bestSuit : null;
    }

    public Card chooseDiscard(List<Card> hand, Suit trumpSuit) {
        return hand.stream()
                .min(Comparator.comparingInt(card -> rulesService.cardStrength(card, trumpSuit, trumpSuit)))
                .orElseThrow();
    }

    public Card chooseCard(List<Card> hand, List<PlayedCard> currentTrick, Suit trumpSuit) {
        List<Card> legalCards = hand.stream()
                .filter(card -> isLegal(hand, currentTrick, card, trumpSuit))
                .toList();
        if (currentTrick.isEmpty()) {
            return legalCards.stream()
                    .max(Comparator.comparingInt(card -> rulesService.cardStrength(card, rulesService.effectiveSuit(card, trumpSuit), trumpSuit)))
                    .orElseThrow();
        }
        Suit ledSuit = rulesService.effectiveSuit(currentTrick.getFirst().card(), trumpSuit);
        return legalCards.stream()
                .min(Comparator.comparingInt(card -> rulesService.cardStrength(card, ledSuit, trumpSuit)))
                .orElseThrow();
    }

    private int trumpScore(List<Card> hand, Suit suit) {
        int score = 0;
        for (Card card : hand) {
            if (rulesService.isRightBower(card, suit)) {
                score += 45;
            } else if (rulesService.isLeftBower(card, suit)) {
                score += 35;
            } else if (rulesService.effectiveSuit(card, suit) == suit) {
                score += switch (card.rank()) {
                    case ACE -> 25;
                    case KING -> 18;
                    case QUEEN -> 14;
                    case JACK -> 12;
                    case TEN -> 8;
                    case NINE -> 6;
                };
            } else if (card.rank() == Rank.ACE) {
                score += 13;
            }
        }
        return score;
    }

    private boolean isLegal(List<Card> hand, List<PlayedCard> currentTrick, Card card, Suit trumpSuit) {
        try {
            rulesService.validateCardPlay(hand, currentTrick, card, trumpSuit);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
