package com.euchre.platform.domain;

public enum Rank {
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    private final String symbol;

    Rank(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public static Rank fromSymbol(String symbol) {
        for (Rank rank : values()) {
            if (rank.symbol.equalsIgnoreCase(symbol)) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Unknown rank symbol: " + symbol);
    }
}
