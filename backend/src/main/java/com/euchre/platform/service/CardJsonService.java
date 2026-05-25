package com.euchre.platform.service;

import com.euchre.platform.domain.Card;
import com.euchre.platform.domain.PlayedCard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CardJsonService {
    private final ObjectMapper objectMapper;

    public CardJsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String writeCards(List<Card> cards) {
        return write(cards.stream().map(Card::code).toList());
    }

    public List<Card> readCards(String json) {
        return read(json, new TypeReference<List<String>>() {}).stream()
                .map(Card::parse)
                .toList();
    }

    public String writePlayedCards(List<PlayedCard> playedCards) {
        List<Map<String, Object>> payload = playedCards.stream()
                .map(card -> Map.<String, Object>of("seatPosition", card.seatPosition(), "card", card.card().code()))
                .toList();
        return write(payload);
    }

    public List<PlayedCard> readPlayedCards(String json) {
        return read(json, new TypeReference<List<Map<String, Object>>>() {}).stream()
                .map(item -> new PlayedCard((Integer) item.get("seatPosition"), Card.parse((String) item.get("card"))))
                .toList();
    }

    public String writeObject(Object value) {
        return write(value);
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize game state.", ex);
        }
    }

    private <T> T read(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read game state.", ex);
        }
    }
}
