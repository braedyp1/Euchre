package com.euchre.platform.dto;

import com.euchre.platform.domain.Suit;

import java.util.List;

public record LegalActionsDto(
        List<String> playableCards,
        List<Suit> trumpSuits,
        boolean canPass,
        boolean canGoAlone
) {
}
