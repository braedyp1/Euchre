package com.euchre.platform.service;

import com.euchre.platform.dto.LoginRequest;
import com.euchre.platform.dto.LoginResponse;
import com.euchre.platform.entity.PlayerEntity;
import com.euchre.platform.entity.SessionTokenEntity;
import com.euchre.platform.exception.ApiException;
import com.euchre.platform.repository.PlayerRepository;
import com.euchre.platform.repository.SessionTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private final PlayerRepository playerRepository;
    private final SessionTokenRepository sessionTokenRepository;

    public AuthService(PlayerRepository playerRepository, SessionTokenRepository sessionTokenRepository) {
        this.playerRepository = playerRepository;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        PlayerEntity player = playerRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> createPlayer(username));

        SessionTokenEntity sessionToken = new SessionTokenEntity();
        sessionToken.setPlayer(player);
        sessionToken.setToken(UUID.randomUUID().toString());
        sessionTokenRepository.save(sessionToken);

        return new LoginResponse(player.getId(), player.getUsername(), sessionToken.getToken());
    }

    @Transactional
    public PlayerEntity requirePlayer(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Session token is required.");
        }
        SessionTokenEntity session = sessionTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid session token."));
        session.setLastUsedAt(Instant.now());
        return session.getPlayer();
    }

    private PlayerEntity createPlayer(String username) {
        PlayerEntity player = new PlayerEntity();
        player.setUsername(username);
        return playerRepository.save(player);
    }
}
