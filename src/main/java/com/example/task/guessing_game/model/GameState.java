package com.example.task.guessing_game.model;

import java.util.ArrayList;
import java.util.List;

// Using a record for a simple, immutable data carrier
public record GameState(
        String playerName,
        String secretNumber,
        int triesLeft,
        List<String> history) {
    // Factory method to create a new game
    public static GameState newGame(String playerName, String secretNumber) {
        return new GameState(playerName, secretNumber, 8, new ArrayList<>());
    }
}