package com.example.task.guessing_game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
public class GameHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String playerName;
    private String secretNumber;
    private boolean won;
    private int guessesMade;
    private LocalDateTime gameDate;

    public GameHistoryEntity() {}

    public GameHistoryEntity(String playerName, String secretNumber, boolean won, int guessesMade, LocalDateTime gameDate) {
        this.playerName = playerName;
        this.secretNumber = secretNumber;
        this.won = won;
        this.guessesMade = guessesMade;
        this.gameDate = gameDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getSecretNumber() {
        return secretNumber;
    }

    public void setSecretNumber(String secretNumber) {
        this.secretNumber = secretNumber;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public int getGuessesMade() {
        return guessesMade;
    }

    public void setGuessesMade(int guessesMade) {
        this.guessesMade = guessesMade;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }
}