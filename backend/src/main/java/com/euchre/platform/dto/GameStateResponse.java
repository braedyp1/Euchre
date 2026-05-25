package com.euchre.platform.dto;

import com.euchre.platform.domain.GamePhase;
import com.euchre.platform.domain.GameStatus;
import com.euchre.platform.domain.Suit;
import com.euchre.platform.domain.Team;

import java.util.List;
import java.util.Map;

public record GameStateResponse(
        Long id,
        GameStatus gameStatus,
        GamePhase phase,
        int dealerPosition,
        int currentTurn,
        Suit trumpSuit,
        Suit turnedDownSuit,
        String upcard,
        int scoreTeamA,
        int scoreTeamB,
        int tricksTeamA,
        int tricksTeamB,
        int currentTrickNumber,
        Team makerTeam,
        Integer makerSeat,
        boolean loneHand,
        String winner,
        List<String> playerHand,
        List<PlayedCardDto> currentTrick,
        Suit leadSuit,
        Integer currentWinningSeat,
        Map<Integer, Integer> handSizes,
        LegalActionsDto legalActions
) {
}
