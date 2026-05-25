package com.euchre.platform.service;

import com.euchre.platform.domain.Card;
import com.euchre.platform.domain.PlayedCard;
import com.euchre.platform.domain.Suit;
import com.euchre.platform.domain.Team;
import com.euchre.platform.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RulesServiceTest {
    private final RulesService rulesService = new RulesService();

    @Test
    void leftBowerCountsAsTrumpSuit() {
        Card jackDiamonds = Card.parse("JD");

        assertThat(rulesService.effectiveSuit(jackDiamonds, Suit.HEARTS)).isEqualTo(Suit.HEARTS);
    }

    @Test
    void playerMustFollowEffectiveLedSuit() {
        List<Card> hand = List.of(Card.parse("JD"), Card.parse("AS"));
        List<PlayedCard> trick = List.of(new PlayedCard(1, Card.parse("AH")));

        assertThatThrownBy(() -> rulesService.validateCardPlay(hand, trick, Card.parse("AS"), Suit.HEARTS))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("follow suit");
    }

    @Test
    void rightBowerBeatsLeftBowerAndAceTrump() {
        List<PlayedCard> trick = List.of(
                new PlayedCard(0, Card.parse("AH")),
                new PlayedCard(1, Card.parse("JD")),
                new PlayedCard(2, Card.parse("JH"))
        );

        assertThat(rulesService.determineTrickWinner(trick, Suit.HEARTS)).isEqualTo(2);
    }

    @Test
    void loneSweepScoresFourForMaker() {
        assertThat(rulesService.makerScore(Team.TEAM_A, 5, 0, true)).isEqualTo(4);
    }

    @Test
    void euchredMakerScoresDefendersTwo() {
        assertThat(rulesService.makerScore(Team.TEAM_A, 2, 3, false)).isZero();
        assertThat(rulesService.defenderScore(Team.TEAM_A, 2, 3)).isEqualTo(2);
    }
}
