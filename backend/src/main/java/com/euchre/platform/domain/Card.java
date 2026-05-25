package com.euchre.platform.domain;

import java.util.Locale;

public record Card(Rank rank, Suit suit) {
    public String code() {
        return rank.symbol() + suit.symbol();
    }

    public static Card parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Card is required.");
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        String suitPart = value.substring(value.length() - 1);
        String rankPart = value.substring(0, value.length() - 1);
        return new Card(Rank.fromSymbol(rankPart), Suit.fromSymbol(suitPart));
    }
}
