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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service for the Guessing Game.
 * Handles game logic (secret number generation, guess checking),
 * player management (guest and OAuth2 flows, name conflict resolution),
 * game result recording, and leaderboard retrieval.
 */
@Service
public class GameService {
    private final PlayerStatsRepository playerStatsRepository;
    private final GameHistoryRepository gameHistoryRepository;

    @Autowired
    public GameService(PlayerStatsRepository playerStatsRepository, GameHistoryRepository gameHistoryRepository) {
        this.playerStatsRepository = playerStatsRepository;
        this.gameHistoryRepository = gameHistoryRepository;
    }

    public record GuessResult(int m, int p) {
        @Override
        public String toString() {
            return "M:" + m + "; P:" + p;
        }
    }

    public String generateSecretNumber() {
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i <= 9; i++) digits.add(i);
        Collections.shuffle(digits);
        StringBuilder secretNumber = new StringBuilder();
        for (int i = 0; i < 4; i++) secretNumber.append(digits.get(i));
        return secretNumber.toString();
    }

    public GuessResult checkGuess(String secret, String guess) {
        int p = 0;
        int m = 0;
        int[] secretDigitCounts = new int[10];
        int[] guessDigitCounts = new int[10];

        for (int i = 0; i < 4; i++) {
            char secretChar = secret.charAt(i);
            char guessChar = guess.charAt(i);
            if (secretChar == guessChar) {
                p++;
            } else {
                secretDigitCounts[secretChar - '0']++;
                guessDigitCounts[guessChar - '0']++;
            }
        }
        for (int i = 0; i < 10; i++) {
            m += Math.min(secretDigitCounts[i], guessDigitCounts[i]);
        }
        return new GuessResult(m, p);
    }

    /**
     * Creates a new GUEST PlayerStatsEntity. Deletes any existing GUEST records
     * with the same name first so there are no duplicates on the leaderboard.
     */
    @Transactional
    public PlayerStatsEntity createGuestPlayer(String name) {
        playerStatsRepository.findAllByPlayerNameAndPlayerType(name, "GUEST")
                .forEach(this::deleteGuestPlayer);
        PlayerStatsEntity entity = new PlayerStatsEntity(name, "GUEST", null, 0, 0, 0);
        return playerStatsRepository.save(entity);
    }

    /**
     * Looks up a player by ID. Used by the restart flow.
     */
    public Optional<PlayerStatsEntity> getPlayerById(Long id) {
        return playerStatsRepository.findById(id);
    }

    /**
     * Returns true if at least one GUEST record already has this name.
     */
    public boolean guestNameExists(String name) {
        return !playerStatsRepository.findAllByPlayerNameAndPlayerType(name, "GUEST").isEmpty();
    }

    /**
     * Returns true if an OAUTH2 player already owns this name.
     * Used to block guests from registering a name claimed by a Google account.
     */
    public boolean oauth2NameExists(String name) {
        return playerStatsRepository.findByPlayerNameAndPlayerType(name, "OAUTH2").isPresent();
    }

    /**
     * Finds an existing OAuth2 player by email, or creates a new one.
     * The display name is set separately via assignNameToOAuth2Player.
     */
    @Transactional
    public PlayerStatsEntity findOrCreateOAuth2Player(String email) {
        return playerStatsRepository.findByEmail(email)
                .orElseGet(() -> playerStatsRepository.save(
                        new PlayerStatsEntity(email, "OAUTH2", email, 0, 0, 0)));
    }

    /**
     * Checks if a name is already taken by another OAUTH2 player (not the one with the given email).
     */
    public Optional<PlayerStatsEntity> findOAuth2Conflict(String name, String currentEmail) {
        return playerStatsRepository.findByPlayerNameAndPlayerType(name, "OAUTH2")
                .filter(p -> !currentEmail.equals(p.getEmail()));
    }

    /**
     * Deletes a guest player and all their game history.
     */
    @Transactional
    public void deleteGuestPlayer(PlayerStatsEntity guest) {
        gameHistoryRepository.deleteByPlayerStatsId(guest.getId());
        playerStatsRepository.delete(guest);
    }

    /**
     * Assigns a display name to an OAuth2 player and saves.
     */
    @Transactional
    public PlayerStatsEntity assignNameToOAuth2Player(PlayerStatsEntity player, String name) {
        player.setPlayerName(name);
        return playerStatsRepository.save(player);
    }

    /**
     * If any GUEST players own the given name, deletes them and their history.
     * Called when an OAuth2 user claims a name, giving them priority.
     */
    @Transactional
    public void resolveGuestConflict(String name) {
        playerStatsRepository.findAllByPlayerNameAndPlayerType(name, "GUEST")
                .forEach(this::deleteGuestPlayer);
    }

    /**
     * Records a completed game result. Updates both GameHistory and PlayerStats.
     */
    @Transactional
    public void recordGameResult(Long playerStatsId, String playerName, boolean won,
                                 int guessesMade, String secretNumber) {
        GameHistoryEntity history = new GameHistoryEntity(
                playerStatsId, playerName, secretNumber, won, guessesMade, LocalDateTime.now());
        gameHistoryRepository.save(history);

        playerStatsRepository.findById(playerStatsId).ifPresent(player -> {
            player.setGamesPlayed(player.getGamesPlayed() + 1);
            player.setTotalGuesses(player.getTotalGuesses() + guessesMade);
            if (won) player.setWins(player.getWins() + 1);
            playerStatsRepository.save(player);
        });
    }

    public List<PlayerStats> getLeaderboard(int minGames) {
        List<PlayerStats> list = playerStatsRepository.findAll().stream()
                .map(PlayerStatsEntity::toDomainModel)
                .filter(p -> p.gamesPlayed() >= minGames)
                .collect(Collectors.toList());
        Collections.sort(list);
        return list;
    }

    public List<PlayerStats> getLeaderboard() {
        return getLeaderboard(1);
    }

    public List<GameHistoryEntity> getPlayerGameHistory(Long playerStatsId) {
        return gameHistoryRepository.findByPlayerStatsId(playerStatsId);
    }
}