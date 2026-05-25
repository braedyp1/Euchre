package com.euchre.platform.dto;

import com.euchre.platform.domain.Suit;

public record TrumpRequest(Suit suit, boolean alone) {
}
