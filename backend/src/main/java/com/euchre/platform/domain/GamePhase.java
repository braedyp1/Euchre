package com.euchre.platform.domain;

public enum GamePhase {
    WAITING_FOR_DEAL,
    FIRST_TRUMP_ROUND,
    SECOND_TRUMP_ROUND,
    PLAYING_TRICKS,
    SCORING,
    GAME_COMPLETE
}
