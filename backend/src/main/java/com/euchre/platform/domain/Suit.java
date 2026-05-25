package com.euchre.platform.domain;

public enum Suit {
    CLUBS("C", Color.BLACK),
    DIAMONDS("D", Color.RED),
    HEARTS("H", Color.RED),
    SPADES("S", Color.BLACK);

    private final String symbol;
    private final Color color;

    Suit(String symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String symbol() {
        return symbol;
    }

    public boolean isSameColor(Suit other) {
        return other != null && color == other.color;
    }

    public static Suit fromSymbol(String symbol) {
        for (Suit suit : values()) {
            if (suit.symbol.equalsIgnoreCase(symbol)) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit symbol: " + symbol);
    }

    private enum Color {
        RED,
        BLACK
    }
}
