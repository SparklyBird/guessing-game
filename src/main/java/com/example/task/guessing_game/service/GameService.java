package com.example.task.guessing_game.service;

import com.example.task.guessing_game.model.GameHistoryEntity;
import com.example.task.guessing_game.model.PlayerStats;
import com.example.task.guessing_game.model.PlayerStatsEntity;
import com.example.task.guessing_game.repository.GameHistoryRepository;
import com.example.task.guessing_game.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final PlayerStatsRepository playerStatsRepository;
    private final GameHistoryRepository gameHistoryRepository;

    @Autowired
    public GameService(PlayerStatsRepository playerStatsRepository, GameHistoryRepository gameHistoryRepository) {
        this.playerStatsRepository = playerStatsRepository;
        this.gameHistoryRepository = gameHistoryRepository;
    }

    // Helper class to hold the result of a guess
    public record GuessResult(int m, int p) {
        @Override
        public String toString() {
            return "M:" + m + "; P:" + p;
        }
    }

    // Returns 4-digit secret number
    public String generateSecretNumber() {
        // Create a list using 0-9 digits
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            digits.add(i);
        }
        // Shuffle the list to randomize the order of the digits
        Collections.shuffle(digits);
        // Take the first 4 digits from the shuffled list
        StringBuilder secretNumber = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            secretNumber.append(digits.get(i));
        }
        return secretNumber.toString();
    }

    /**
     * Compares the user's guess with the secret number and calculates M and P
     * @param secret The 4-digit secret number
     * @param guess The 4-digit user guess
     * @return A GuessResult object containing the counts for M and P
     */
    public GuessResult checkGuess(String secret, String guess) {
        int p = 0; // Correct position
        int m = 0; // Correct digit, wrong position
        int[] secretDigitCounts = new int[10];
        int[] guessDigitCounts = new int[10];

        // First, counts all digits that are in the correct position (P)
        // and also count the frequency of all non-P digits
        for (int i = 0; i < 4; i++) {
            char secretChar = secret.charAt(i);
            char guessChar = guess.charAt(i);
            if (secretChar == guessChar) {
                p++;
            } else {
                // Convert char to int ('0' is 48, '1' is 49, etc.)
                secretDigitCounts[secretChar - '0']++;
                guessDigitCounts[guessChar - '0']++;
            }
        }

        // Find the number of common digits between the remaining ones (M)
        for (int i = 0; i < 10; i++) {
            // M is the minimum of the counts for each digit
            m += Math.min(secretDigitCounts[i], guessDigitCounts[i]);
        }

        return new GuessResult(m, p);
    }

    // Method to update player stats after a game
    @Transactional
    public void recordGameResult(String playerName, boolean won, int guessesMade, String secretNumber) {
        // Check if the player is a guest by looking for the "GUEST:" prefix
        boolean isGuest = playerName.startsWith("GUEST:");

        // Save game history for all players, including guests
        GameHistoryEntity gameHistory = new GameHistoryEntity(
                playerName,
                secretNumber,
                won,
                guessesMade,
                LocalDateTime.now()
        );
        gameHistoryRepository.save(gameHistory);

        // Only update player stats for non-guest players
        if (!isGuest) {
            Optional<PlayerStatsEntity> playerStatsOpt = playerStatsRepository.findById(playerName);
            PlayerStatsEntity playerStats;

            if (playerStatsOpt.isPresent()) {
                playerStats = playerStatsOpt.get();
                playerStats.setGamesPlayed(playerStats.getGamesPlayed() + 1);
                playerStats.setTotalGuesses(playerStats.getTotalGuesses() + guessesMade);
                if (won) {
                    playerStats.setWins(playerStats.getWins() + 1);
                }
            } else {
                playerStats = new PlayerStatsEntity(
                        playerName,
                        1, // gamesPlayed
                        guessesMade,
                        won ? 1 : 0 // wins
                );
            }
            playerStatsRepository.save(playerStats);
        }
    }

    // Method to get the sorted leaderboard with minimum games filter
    public List<PlayerStats> getLeaderboard(int minGames) {
        List<PlayerStatsEntity> playerStatsEntities = playerStatsRepository.findAll();
        // Convert to domain model and filter
        List<PlayerStats> playerStatsList = playerStatsEntities.stream()
                .map(PlayerStatsEntity::toDomainModel)
                .filter(player -> player.gamesPlayed() >= minGames)
                .collect(Collectors.toList());
        // Sort the list using the logic defined in the PlayerStats record
        Collections.sort(playerStatsList);
        return playerStatsList;
    }

    // Keep the original method for backward compatibility
    public List<PlayerStats> getLeaderboard() {
        return getLeaderboard(1); // Default minimum games is 1
    }

    // Method to get game history for a player
    public List<GameHistoryEntity> getPlayerGameHistory(String playerName) {
        return gameHistoryRepository.findByPlayerName(playerName);
    }
}