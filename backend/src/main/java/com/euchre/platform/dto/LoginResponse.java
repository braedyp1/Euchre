package com.euchre.platform.dto;

public record LoginResponse(
        Long playerId,
        String username,
        String sessionToken
) {
}
