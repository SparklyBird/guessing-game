package com.example.task.guessing_game.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
public class GameHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long playerStatsId;  // FK to player_stats.id
    private String playerName;   // kept for display purposes
    private String secretNumber;
    private boolean won;
    private int guessesMade;
    private LocalDateTime gameDate;

    public GameHistoryEntity() {}

    public GameHistoryEntity(Long playerStatsId, String playerName, String secretNumber,
                             boolean won, int guessesMade, LocalDateTime gameDate) {
        this.playerStatsId = playerStatsId;
        this.playerName = playerName;
        this.secretNumber = secretNumber;
        this.won = won;
        this.guessesMade = guessesMade;
        this.gameDate = gameDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerStatsId() { return playerStatsId; }
    public void setPlayerStatsId(Long playerStatsId) { this.playerStatsId = playerStatsId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getSecretNumber() { return secretNumber; }
    public void setSecretNumber(String secretNumber) { this.secretNumber = secretNumber; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public int getGuessesMade() { return guessesMade; }
    public void setGuessesMade(int guessesMade) { this.guessesMade = guessesMade; }

    public LocalDateTime getGameDate() { return gameDate; }
    public void setGameDate(LocalDateTime gameDate) { this.gameDate = gameDate; }
}
