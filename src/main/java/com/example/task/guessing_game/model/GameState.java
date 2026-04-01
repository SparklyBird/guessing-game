package com.example.task.guessing_game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record GameState(
        String playerName,
        Long playerStatsId,
        String playerType,    // "GUEST" or "OAUTH2"
        String secretNumber,
        int triesLeft,
        List<String> history) implements Serializable {

    public static GameState newGame(String playerName, Long playerStatsId, String playerType, String secretNumber) {
        return new GameState(playerName, playerStatsId, playerType, secretNumber, 8, new ArrayList<>());
    }
}
