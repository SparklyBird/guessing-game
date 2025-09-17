package com.example.task.guessing_game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_stats")
public class PlayerStatsEntity {
    @Id
    private String playerName;
    private int gamesPlayed;
    private int totalGuesses;
    private int wins;

    public PlayerStatsEntity() {}

    public PlayerStatsEntity(String playerName, int gamesPlayed, int totalGuesses, int wins) {
        this.playerName = playerName;
        this.gamesPlayed = gamesPlayed;
        this.totalGuesses = totalGuesses;
        this.wins = wins;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getTotalGuesses() {
        return totalGuesses;
    }

    public void setTotalGuesses(int totalGuesses) {
        this.totalGuesses = totalGuesses;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    // Convert to domain model
    public PlayerStats toDomainModel() {
        return new PlayerStats(playerName, gamesPlayed, totalGuesses, wins);
    }
}