package com.euchre.platform.service;

import com.euchre.platform.ai.AiService;
import com.euchre.platform.domain.ActionType;
import com.euchre.platform.domain.Card;
import com.euchre.platform.domain.GamePhase;
import com.euchre.platform.domain.GameStatus;
import com.euchre.platform.domain.PlayedCard;
import com.euchre.platform.domain.Suit;
import com.euchre.platform.domain.Team;
import com.euchre.platform.dto.GameStateResponse;
import com.euchre.platform.dto.LegalActionsDto;
import com.euchre.platform.dto.PlayedCardDto;
import com.euchre.platform.entity.ActionLogEntity;
import com.euchre.platform.entity.GameSessionEntity;
import com.euchre.platform.entity.HandStateEntity;
import com.euchre.platform.entity.PlayerEntity;
import com.euchre.platform.entity.TrickStateEntity;
import com.euchre.platform.exception.ApiException;
import com.euchre.platform.repository.ActionLogRepository;
import com.euchre.platform.repository.GameSessionRepository;
import com.euchre.platform.repository.HandStateRepository;
import com.euchre.platform.repository.PlayerRepository;
import com.euchre.platform.repository.TrickStateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Service
public class GameService {
    private static final int HUMAN_SEAT = 0;
    private static final int SCORE_TO_WIN = 10;

    private final GameSessionRepository gameSessionRepository;
    private final HandStateRepository handStateRepository;
    private final TrickStateRepository trickStateRepository;
    private final ActionLogRepository actionLogRepository;
    private final PlayerRepository playerRepository;
    private final RulesService rulesService;
    private final AiService aiService;
    private final CardJsonService cardJsonService;
    private final Random random = new Random();

    public GameService(
            GameSessionRepository gameSessionRepository,
            HandStateRepository handStateRepository,
            TrickStateRepository trickStateRepository,
            ActionLogRepository actionLogRepository,
            PlayerRepository playerRepository,
            RulesService rulesService,
            AiService aiService,
            CardJsonService cardJsonService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.handStateRepository = handStateRepository;
        this.trickStateRepository = trickStateRepository;
        this.actionLogRepository = actionLogRepository;
        this.playerRepository = playerRepository;
        this.rulesService = rulesService;
        this.aiService = aiService;
        this.cardJsonService = cardJsonService;
    }

    @Transactional
    public GameStateResponse startNewGame(PlayerEntity player) {
        GameSessionEntity game = new GameSessionEntity();
        game.setPlayer(player);
        game.setGameStatus(GameStatus.ACTIVE);
        game.setPhase(GamePhase.WAITING_FOR_DEAL);
        game.setDealerPosition(random.nextInt(4));
        game.setCurrentTurn(0);
        game = gameSessionRepository.save(game);

        dealNewHand(game, false);
        log(game, "SERVER", ActionType.GAME_CREATED, Map.of("dealerPosition", game.getDealerPosition()));
        advanceCpuTurns(game);
        return toResponse(game);
    }

    @Transactional(readOnly = true)
    public GameStateResponse latestGame(PlayerEntity player) {
        GameSessionEntity game = gameSessionRepository.findFirstByPlayerIdAndGameStatusOrderByUpdatedAtDesc(player.getId(), GameStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "No unfinished game exists."));
        return toResponse(game);
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGame(PlayerEntity player, Long gameId) {
        GameSessionEntity game = requireOwnedGame(player, gameId, false);
        return toResponse(game);
    }

    @Transactional
    public GameStateResponse playCard(PlayerEntity player, Long gameId, String cardCode) {
        GameSessionEntity game = requireOwnedGame(player, gameId, true);
        requireActiveHumanTurn(game, GamePhase.PLAYING_TRICKS);

        Card card = parseCard(cardCode);
        playCardForCurrentSeat(game, card, false);
        log(game, "HUMAN", ActionType.CARD_PLAYED, Map.of("card", card.code()));
        advanceCpuTurns(game);
        return toResponse(game);
    }

    @Transactional
    public GameStateResponse selectTrump(PlayerEntity player, Long gameId, Suit suit, boolean alone) {
        GameSessionEntity game = requireOwnedGame(player, gameId, true);
        if (suit == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Trump suit is required.");
        }
        requireHumanTrumpTurn(game);

        chooseTrump(game, HUMAN_SEAT, suit, alone, true);
        log(game, "HUMAN", ActionType.TRUMP_SELECTED, Map.of("suit", suit, "alone", alone));
        advanceCpuTurns(game);
        return toResponse(game);
    }

    @Transactional
    public GameStateResponse passTrump(PlayerEntity player, Long gameId) {
        GameSessionEntity game = requireOwnedGame(player, gameId, true);
        requireHumanTrumpTurn(game);
        if (isStickDealerDecision(game)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Stick the dealer requires the dealer to choose trump.");
        }

        handleTrumpPass(game, false);
        log(game, "HUMAN", ActionType.TRUMP_PASSED, Map.of("phase", game.getPhase()));
        advanceCpuTurns(game);
        return toResponse(game);
    }

    @Transactional
    public void abandonGame(PlayerEntity player, Long gameId) {
        GameSessionEntity game = requireOwnedGame(player, gameId, true);
        if (game.getGameStatus() == GameStatus.COMPLETE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Completed games cannot be abandoned.");
        }
        game.setGameStatus(GameStatus.ABANDONED);
        game.touchActionTimestamp();
        log(game, "HUMAN", ActionType.GAME_ABANDONED, Map.of());
    }

    private void dealNewHand(GameSessionEntity game, boolean advanceDealer) {
        if (advanceDealer) {
            game.setDealerPosition(next(game.getDealerPosition()));
        }
        game.setPhase(GamePhase.FIRST_TRUMP_ROUND);
        game.setCurrentTurn(next(game.getDealerPosition()));
        game.setTrumpSuit(null);
        game.setTurnedDownSuit(null);
        game.setMakerTeam(null);
        game.setMakerSeat(null);
        game.setLoneHand(false);
        game.setTricksTeamA(0);
        game.setTricksTeamB(0);
        game.setCurrentTrickNumber(1);
        game.setPassesThisRound(0);
        game.setWinner(null);

        List<Card> deck = new ArrayList<>(rulesService.createDeck());
        Collections.shuffle(deck);

        handStateRepository.deleteByGameSessionId(game.getId());
        trickStateRepository.deleteByGameSessionId(game.getId());

        for (int seat = 0; seat < 4; seat++) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                hand.add(deck.removeFirst());
            }
            saveHand(game, seat, hand);
        }
        game.setUpcard(deck.removeFirst().code());
        saveTrick(game, List.of());
        game.touchActionTimestamp();
    }

    private void advanceCpuTurns(GameSessionEntity game) {
        int guard = 0;
        while (game.getGameStatus() == GameStatus.ACTIVE && game.getCurrentTurn() != HUMAN_SEAT && guard++ < 100) {
            if (game.getPhase() == GamePhase.FIRST_TRUMP_ROUND) {
                List<Card> hand = handFor(game, game.getCurrentTurn());
                Suit upcardSuit = Card.parse(game.getUpcard()).suit();
                if (aiService.shouldOrderUp(hand, upcardSuit)) {
                    chooseTrump(game, game.getCurrentTurn(), upcardSuit, false, false);
                    log(game, "CPU_" + game.getMakerSeat(), ActionType.AI_DECISION, Map.of("decision", "ORDER_UP", "suit", upcardSuit));
                } else {
                    handleTrumpPass(game, true);
                }
            } else if (game.getPhase() == GamePhase.SECOND_TRUMP_ROUND) {
                List<Card> hand = handFor(game, game.getCurrentTurn());
                Suit chosen = aiService.chooseTrump(hand, game.getTurnedDownSuit(), isStickDealerDecision(game));
                if (chosen == null) {
                    handleTrumpPass(game, true);
                } else {
                    chooseTrump(game, game.getCurrentTurn(), chosen, false, false);
                    log(game, "CPU_" + game.getMakerSeat(), ActionType.AI_DECISION, Map.of("decision", "CHOOSE_TRUMP", "suit", chosen));
                }
            } else if (game.getPhase() == GamePhase.PLAYING_TRICKS) {
                int actorSeat = game.getCurrentTurn();
                List<Card> hand = handFor(game, game.getCurrentTurn());
                List<PlayedCard> trick = currentTrick(game);
                Card card = aiService.chooseCard(hand, trick, game.getTrumpSuit());
                playCardForCurrentSeat(game, card, true);
                log(game, "CPU_" + actorSeat, ActionType.AI_DECISION, Map.of("decision", "PLAY_CARD", "card", card.code()));
            } else if (game.getPhase() == GamePhase.SCORING) {
                scoreHand(game);
            } else {
                break;
            }
        }
        if (guard >= 100) {
            game.setGameStatus(GameStatus.CORRUPTED);
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", "CPU turn processing exceeded safety limit.");
        }
    }

    private void handleTrumpPass(GameSessionEntity game, boolean cpuAction) {
        if (game.getPhase() != GamePhase.FIRST_TRUMP_ROUND && game.getPhase() != GamePhase.SECOND_TRUMP_ROUND) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHASE", "Trump decisions are not available now.");
        }
        game.setPassesThisRound(game.getPassesThisRound() + 1);
        if (cpuAction) {
            log(game, "CPU_" + game.getCurrentTurn(), ActionType.AI_DECISION, Map.of("decision", "PASS", "phase", game.getPhase()));
        }

        if (game.getPassesThisRound() < 4) {
            game.setCurrentTurn(next(game.getCurrentTurn()));
            game.touchActionTimestamp();
            return;
        }

        if (game.getPhase() == GamePhase.FIRST_TRUMP_ROUND) {
            game.setTurnedDownSuit(Card.parse(game.getUpcard()).suit());
            game.setPhase(GamePhase.SECOND_TRUMP_ROUND);
            game.setCurrentTurn(next(game.getDealerPosition()));
            game.setPassesThisRound(0);
        } else {
            Suit forcedSuit = aiService.chooseTrump(handFor(game, game.getDealerPosition()), game.getTurnedDownSuit(), true);
            chooseTrump(game, game.getDealerPosition(), forcedSuit, false, false);
        }
        game.touchActionTimestamp();
    }

    private void chooseTrump(GameSessionEntity game, int makerSeat, Suit suit, boolean alone, boolean humanAction) {
        if (game.getPhase() == GamePhase.FIRST_TRUMP_ROUND) {
            Suit upcardSuit = Card.parse(game.getUpcard()).suit();
            if (suit != upcardSuit) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Round one can only order up the upcard suit.");
            }
            List<Card> dealerHand = new ArrayList<>(handFor(game, game.getDealerPosition()));
            dealerHand.add(Card.parse(game.getUpcard()));
            Card discard = aiService.chooseDiscard(dealerHand, suit);
            dealerHand.remove(discard);
            saveHand(game, game.getDealerPosition(), dealerHand);
        } else if (game.getPhase() == GamePhase.SECOND_TRUMP_ROUND) {
            if (suit == game.getTurnedDownSuit()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Round two must choose a suit other than the turned-down suit.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHASE", "Trump selection is not available now.");
        }

        if (alone && (!humanAction || makerSeat != HUMAN_SEAT)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Only the human player may go alone.");
        }

        game.setTrumpSuit(suit);
        game.setMakerSeat(makerSeat);
        game.setMakerTeam(Team.forSeat(makerSeat));
        game.setLoneHand(alone);
        game.setPhase(GamePhase.PLAYING_TRICKS);
        game.setCurrentTurn(nextActiveSeat(game, game.getDealerPosition()));
        game.setPassesThisRound(0);
        game.touchActionTimestamp();
    }

    private void playCardForCurrentSeat(GameSessionEntity game, Card card, boolean cpuAction) {
        if (game.getPhase() != GamePhase.PLAYING_TRICKS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHASE", "Cards can only be played during trick play.");
        }
        int seat = game.getCurrentTurn();
        List<Card> hand = new ArrayList<>(handFor(game, seat));
        List<PlayedCard> trick = new ArrayList<>(currentTrick(game));
        rulesService.validateCardPlay(hand, trick, card, game.getTrumpSuit());

        hand.remove(card);
        trick.add(new PlayedCard(seat, card));
        saveHand(game, seat, hand);
        saveTrick(game, trick);

        int completeSize = activeSeats(game).size();
        if (trick.size() == completeSize) {
            finishTrick(game, trick);
        } else {
            game.setCurrentTurn(nextActiveSeat(game, seat));
        }
        game.touchActionTimestamp();
    }

    private void finishTrick(GameSessionEntity game, List<PlayedCard> trick) {
        int winnerSeat = rulesService.determineTrickWinner(trick, game.getTrumpSuit());

        TrickStateEntity trickState = currentTrickEntity(game);
        trickState.setWinningSeat(winnerSeat);
        trickState.setLeadSuit(rulesService.effectiveSuit(trick.getFirst().card(), game.getTrumpSuit()));
        trickStateRepository.save(trickState);

        if (Team.forSeat(winnerSeat) == Team.TEAM_A) {
            game.setTricksTeamA(game.getTricksTeamA() + 1);
        } else {
            game.setTricksTeamB(game.getTricksTeamB() + 1);
        }

        if (game.getCurrentTrickNumber() == 5) {
            game.setPhase(GamePhase.SCORING);
            scoreHand(game);
        } else {
            game.setCurrentTrickNumber(game.getCurrentTrickNumber() + 1);
            game.setCurrentTurn(winnerSeat);
            saveTrick(game, List.of());
        }
    }

    private void scoreHand(GameSessionEntity game) {
        Team makerTeam = Objects.requireNonNull(game.getMakerTeam());
        int makerScore = rulesService.makerScore(makerTeam, game.getTricksTeamA(), game.getTricksTeamB(), game.isLoneHand());
        int defenderScore = rulesService.defenderScore(makerTeam, game.getTricksTeamA(), game.getTricksTeamB());
        Team defenderTeam = makerTeam.other();

        if (makerTeam == Team.TEAM_A) {
            game.setScoreTeamA(game.getScoreTeamA() + makerScore);
        } else {
            game.setScoreTeamB(game.getScoreTeamB() + makerScore);
        }
        if (defenderTeam == Team.TEAM_A) {
            game.setScoreTeamA(game.getScoreTeamA() + defenderScore);
        } else {
            game.setScoreTeamB(game.getScoreTeamB() + defenderScore);
        }
        log(game, "SERVER", ActionType.HAND_SCORED, Map.of("teamATricks", game.getTricksTeamA(), "teamBTricks", game.getTricksTeamB()));

        if (game.getScoreTeamA() >= SCORE_TO_WIN || game.getScoreTeamB() >= SCORE_TO_WIN) {
            game.setPhase(GamePhase.GAME_COMPLETE);
            game.setGameStatus(GameStatus.COMPLETE);
            game.setWinner(game.getScoreTeamA() >= SCORE_TO_WIN ? "TEAM_A" : "TEAM_B");
            updatePlayerStats(game);
            log(game, "SERVER", ActionType.GAME_COMPLETED, Map.of("winner", game.getWinner()));
        } else {
            dealNewHand(game, true);
        }
    }

    private void updatePlayerStats(GameSessionEntity game) {
        PlayerEntity player = game.getPlayer();
        player.setTotalGamesPlayed(player.getTotalGamesPlayed() + 1);
        if ("TEAM_A".equals(game.getWinner())) {
            player.setTotalWins(player.getTotalWins() + 1);
        } else {
            player.setTotalLosses(player.getTotalLosses() + 1);
        }
        playerRepository.save(player);
    }

    private GameSessionEntity requireOwnedGame(PlayerEntity player, Long gameId, boolean lock) {
        GameSessionEntity game = (lock ? gameSessionRepository.findWithLockById(gameId) : gameSessionRepository.findById(gameId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Game not found."));
        if (!game.getPlayer().getId().equals(player.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Game does not belong to this session.");
        }
        return game;
    }

    private void requireActiveHumanTurn(GameSessionEntity game, GamePhase phase) {
        if (game.getGameStatus() != GameStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "Game is not active.");
        }
        if (game.getPhase() != phase) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHASE", "Action is not available in this phase.");
        }
        if (game.getCurrentTurn() != HUMAN_SEAT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MOVE", "It is not your turn.");
        }
    }

    private void requireHumanTrumpTurn(GameSessionEntity game) {
        if (game.getPhase() != GamePhase.FIRST_TRUMP_ROUND && game.getPhase() != GamePhase.SECOND_TRUMP_ROUND) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHASE", "Trump selection is not available now.");
        }
        requireActiveHumanTurn(game, game.getPhase());
    }

    private Card parseCard(String cardCode) {
        try {
            return Card.parse(cardCode);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CARD", ex.getMessage());
        }
    }

    private List<Card> handFor(GameSessionEntity game, int seat) {
        return cardJsonService.readCards(handStateRepository.findByGameSessionIdAndSeatPosition(game.getId(), seat)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", "Missing hand state."))
                .getSerializedCards());
    }

    private void saveHand(GameSessionEntity game, int seat, List<Card> cards) {
        HandStateEntity hand = handStateRepository.findByGameSessionIdAndSeatPosition(game.getId(), seat)
                .orElseGet(() -> {
                    HandStateEntity created = new HandStateEntity();
                    created.setGameSession(game);
                    created.setSeatPosition(seat);
                    return created;
                });
        hand.setSerializedCards(cardJsonService.writeCards(cards));
        handStateRepository.save(hand);
    }

    private List<PlayedCard> currentTrick(GameSessionEntity game) {
        return cardJsonService.readPlayedCards(currentTrickEntity(game).getPlayedCards());
    }

    private TrickStateEntity currentTrickEntity(GameSessionEntity game) {
        return trickStateRepository.findByGameSessionIdAndTrickNumber(game.getId(), game.getCurrentTrickNumber())
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", "Missing trick state."));
    }

    private void saveTrick(GameSessionEntity game, List<PlayedCard> playedCards) {
        TrickStateEntity trick = trickStateRepository.findByGameSessionIdAndTrickNumber(game.getId(), game.getCurrentTrickNumber())
                .orElseGet(() -> {
                    TrickStateEntity created = new TrickStateEntity();
                    created.setGameSession(game);
                    created.setTrickNumber(game.getCurrentTrickNumber());
                    return created;
                });
        trick.setPlayedCards(cardJsonService.writePlayedCards(playedCards));
        trick.setLeadSuit(playedCards.isEmpty() ? null : rulesService.effectiveSuit(playedCards.getFirst().card(), game.getTrumpSuit()));
        trick.setWinningSeat(playedCards.isEmpty() ? null : rulesService.determineTrickWinner(playedCards, game.getTrumpSuit()));
        trickStateRepository.save(trick);
    }

    private List<Integer> activeSeats(GameSessionEntity game) {
        if (!game.isLoneHand()) {
            return List.of(0, 1, 2, 3);
        }
        int partnerSeat = partnerOf(Objects.requireNonNull(game.getMakerSeat()));
        return List.of(0, 1, 2, 3).stream()
                .filter(seat -> seat != partnerSeat)
                .toList();
    }

    private int partnerOf(int seat) {
        return (seat + 2) % 4;
    }

    private int nextActiveSeat(GameSessionEntity game, int seat) {
        int nextSeat = seat;
        do {
            nextSeat = next(nextSeat);
        } while (!activeSeats(game).contains(nextSeat));
        return nextSeat;
    }

    private int next(int seat) {
        return (seat + 1) % 4;
    }

    private boolean isStickDealerDecision(GameSessionEntity game) {
        return game.getPhase() == GamePhase.SECOND_TRUMP_ROUND
                && game.getCurrentTurn() == game.getDealerPosition()
                && game.getPassesThisRound() == 3;
    }

    private GameStateResponse toResponse(GameSessionEntity game) {
        List<Card> humanHand = handFor(game, HUMAN_SEAT);
        TrickStateEntity trickState = currentTrickEntity(game);
        List<PlayedCard> trick = cardJsonService.readPlayedCards(trickState.getPlayedCards());
        List<HandStateEntity> hands = handStateRepository.findByGameSessionIdOrderBySeatPosition(game.getId());

        Map<Integer, Integer> handSizes = new HashMap<>();
        for (HandStateEntity hand : hands) {
            handSizes.put(hand.getSeatPosition(), cardJsonService.readCards(hand.getSerializedCards()).size());
        }

        return new GameStateResponse(
                game.getId(),
                game.getGameStatus(),
                game.getPhase(),
                game.getDealerPosition(),
                game.getCurrentTurn(),
                game.getTrumpSuit(),
                game.getTurnedDownSuit(),
                game.getUpcard(),
                game.getScoreTeamA(),
                game.getScoreTeamB(),
                game.getTricksTeamA(),
                game.getTricksTeamB(),
                game.getCurrentTrickNumber(),
                game.getMakerTeam(),
                game.getMakerSeat(),
                game.isLoneHand(),
                game.getWinner(),
                humanHand.stream().map(Card::code).toList(),
                trick.stream().map(played -> new PlayedCardDto(played.seatPosition(), played.card().code())).toList(),
                trickState.getLeadSuit(),
                trickState.getWinningSeat(),
                handSizes,
                legalActions(game, humanHand, trick)
        );
    }

    private LegalActionsDto legalActions(GameSessionEntity game, List<Card> humanHand, List<PlayedCard> trick) {
        if (game.getGameStatus() != GameStatus.ACTIVE || game.getCurrentTurn() != HUMAN_SEAT) {
            return new LegalActionsDto(List.of(), List.of(), false, false);
        }
        if (game.getPhase() == GamePhase.PLAYING_TRICKS) {
            List<String> playable = humanHand.stream()
                    .filter(card -> {
                        try {
                            rulesService.validateCardPlay(humanHand, trick, card, game.getTrumpSuit());
                            return true;
                        } catch (ApiException ex) {
                            return false;
                        }
                    })
                    .map(Card::code)
                    .toList();
            return new LegalActionsDto(playable, List.of(), false, false);
        }
        if (game.getPhase() == GamePhase.FIRST_TRUMP_ROUND) {
            Suit upcardSuit = Card.parse(game.getUpcard()).suit();
            return new LegalActionsDto(List.of(), List.of(upcardSuit), true, true);
        }
        if (game.getPhase() == GamePhase.SECOND_TRUMP_ROUND) {
            List<Suit> suits = EnumSet.allOf(Suit.class).stream()
                    .filter(suit -> suit != game.getTurnedDownSuit())
                    .toList();
            return new LegalActionsDto(List.of(), suits, !isStickDealerDecision(game), true);
        }
        return new LegalActionsDto(List.of(), List.of(), false, false);
    }

    private void log(GameSessionEntity game, String actor, ActionType actionType, Object payload) {
        ActionLogEntity log = new ActionLogEntity();
        log.setGameSession(game);
        log.setActor(actor);
        log.setActionType(actionType);
        log.setPayload(cardJsonService.writeObject(payload));
        actionLogRepository.save(log);
    }
}
