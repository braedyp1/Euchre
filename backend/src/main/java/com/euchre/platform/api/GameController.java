package com.euchre.platform.api;

import com.euchre.platform.dto.GameStateResponse;
import com.euchre.platform.dto.PlayCardRequest;
import com.euchre.platform.dto.TrumpRequest;
import com.euchre.platform.entity.PlayerEntity;
import com.euchre.platform.service.AuthService;
import com.euchre.platform.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private static final String SESSION_HEADER = "X-Session-Token";

    private final AuthService authService;
    private final GameService gameService;

    public GameController(AuthService authService, GameService gameService) {
        this.authService = authService;
        this.gameService = gameService;
    }

    @PostMapping("/new")
    public GameStateResponse newGame(@RequestHeader(SESSION_HEADER) String token) {
        PlayerEntity player = authService.requirePlayer(token);
        return gameService.startNewGame(player);
    }

    @GetMapping("/latest")
    public GameStateResponse latest(@RequestHeader(SESSION_HEADER) String token) {
        PlayerEntity player = authService.requirePlayer(token);
        return gameService.latestGame(player);
    }

    @GetMapping("/{id}")
    public GameStateResponse getGame(@RequestHeader(SESSION_HEADER) String token, @PathVariable Long id) {
        PlayerEntity player = authService.requirePlayer(token);
        return gameService.getGame(player, id);
    }

    @PostMapping("/{id}/play")
    public GameStateResponse playCard(
            @RequestHeader(SESSION_HEADER) String token,
            @PathVariable Long id,
            @Valid @RequestBody PlayCardRequest request
    ) {
        PlayerEntity player = authService.requirePlayer(token);
        return gameService.playCard(player, id, request.card());
    }

    @PostMapping("/{id}/trump")
    public GameStateResponse selectTrump(
            @RequestHeader(SESSION_HEADER) String token,
            @PathVariable Long id,
            @RequestBody TrumpRequest request
    ) {
        PlayerEntity player = authService.requirePlayer(token);
        return gameService.selectTrump(player, id, request.suit(), request.alone());
    }

    @PostMapping("/{id}/pass")
    public GameStateResponse passTrump(@RequestHeader(SESSION_HEADER) String token, @PathVariable Long id) {
        PlayerEntity player = authService.requirePlayer(token);
        return gameService.passTrump(player, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> abandonGame(@RequestHeader(SESSION_HEADER) String token, @PathVariable Long id) {
        PlayerEntity player = authService.requirePlayer(token);
        gameService.abandonGame(player, id);
        return ResponseEntity.noContent().build();
    }
}
