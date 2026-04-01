package com.example.task.guessing_game.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_stats")
public class PlayerStatsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private String playerType; // "GUEST" or "OAUTH2"
    private String email;      // non-null for OAuth2 players, null for guests
    private int gamesPlayed;
    private int totalGuesses;
    private int wins;
    private LocalDateTime createdAt;

    public PlayerStatsEntity() {}

    public PlayerStatsEntity(String playerName, String playerType, String email,
                              int gamesPlayed, int totalGuesses, int wins) {
        this.playerName = playerName;
        this.playerType = playerType;
        this.email = email;
        this.gamesPlayed = gamesPlayed;
        this.totalGuesses = totalGuesses;
        this.wins = wins;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getPlayerType() { return playerType; }
    public void setPlayerType(String playerType) { this.playerType = playerType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getTotalGuesses() { return totalGuesses; }
    public void setTotalGuesses(int totalGuesses) { this.totalGuesses = totalGuesses; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public PlayerStats toDomainModel() {
        return new PlayerStats(playerName, playerType, gamesPlayed, totalGuesses, wins);
    }
}
