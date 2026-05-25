package com.euchre.platform.domain;

import java.util.ArrayList;
import java.util.List;

public class Trick {
    private final int trickNumber;
    private final List<PlayedCard> playedCards;

    public Trick(int trickNumber, List<PlayedCard> playedCards) {
        this.trickNumber = trickNumber;
        this.playedCards = new ArrayList<>(playedCards);
    }

    public int trickNumber() {
        return trickNumber;
    }

    public List<PlayedCard> playedCards() {
        return playedCards;
    }

    public boolean isComplete() {
        return playedCards.size() == 4;
    }
}
