package com.example.task.guessing_game.service;

import com.example.task.guessing_game.model.PlayerStatsEntity;
import com.example.task.guessing_game.repository.GameHistoryRepository;
import com.example.task.guessing_game.repository.PlayerStatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private PlayerStatsRepository playerStatsRepository;

    @Mock
    private GameHistoryRepository gameHistoryRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    public void testGenerateSecretNumber() {
        String secretNumber = gameService.generateSecretNumber();
        assertEquals(4, secretNumber.length());
        assertEquals(4, secretNumber.chars().distinct().count());
    }

    @Test
    public void testCheckGuess() {
        // No matches
        GameService.GuessResult result1 = gameService.checkGuess("1234", "5678");
        assertEquals(0, result1.m());
        assertEquals(0, result1.p());
        // One digit correct position
        GameService.GuessResult result2 = gameService.checkGuess("1234", "1567");
        assertEquals(0, result2.m());
        assertEquals(1, result2.p());
        // One digit wrong position
        GameService.GuessResult result3 = gameService.checkGuess("1234", "5123");
        assertEquals(3, result3.m());
        assertEquals(0, result3.p());
        // Two correct position, one wrong
        GameService.GuessResult result4 = gameService.checkGuess("1234", "1243");
        assertEquals(2, result4.m());
        assertEquals(2, result4.p());
        // All correct
        GameService.GuessResult result5 = gameService.checkGuess("1234", "1234");
        assertEquals(0, result5.m());
        assertEquals(4, result5.p());
    }

    @Test
    public void testRecordGameResult() {
        // Setup: player exists with id=1L
        PlayerStatsEntity existingPlayer = new PlayerStatsEntity("Alice", "OAUTH2", "alice@example.com", 2, 10, 1);
        existingPlayer.setId(1L);
        when(playerStatsRepository.findById(1L)).thenReturn(Optional.of(existingPlayer));
        when(playerStatsRepository.save(any(PlayerStatsEntity.class))).thenAnswer(i -> i.getArgument(0));

        gameService.recordGameResult(1L, "Alice", true, 3, "1234");

        verify(gameHistoryRepository, times(1)).save(any());
        verify(playerStatsRepository, times(1)).save(any());

        // Verify stats were updated
        assertEquals(3, existingPlayer.getGamesPlayed());
        assertEquals(13, existingPlayer.getTotalGuesses());
        assertEquals(2, existingPlayer.getWins());

        reset(playerStatsRepository, gameHistoryRepository);

        // Player not found: stats update is skipped but history is saved
        when(playerStatsRepository.findById(99L)).thenReturn(Optional.empty());
        gameService.recordGameResult(99L, "Ghost", false, 8, "5678");
        verify(gameHistoryRepository, times(1)).save(any());
        verify(playerStatsRepository, never()).save(any());
    }

    @Test
    public void testLeaderboard() {
        PlayerStatsEntity alice = new PlayerStatsEntity("Alice", "OAUTH2", null, 2, 11, 1);
        PlayerStatsEntity bob = new PlayerStatsEntity("Bob", "OAUTH2", null, 2, 9, 2);
        PlayerStatsEntity charlie = new PlayerStatsEntity("Charlie", "OAUTH2", null, 2, 10, 1);
        when(playerStatsRepository.findAll()).thenReturn(List.of(alice, bob, charlie));

        var leaderboard = gameService.getLeaderboard();
        assertEquals("Bob", leaderboard.get(0).playerName());
        assertEquals("Charlie", leaderboard.get(1).playerName());
        assertEquals("Alice", leaderboard.get(2).playerName());
    }

    @Test
    public void testLeaderboardWithMinGamesFilter() {
        PlayerStatsEntity alice = new PlayerStatsEntity("Alice", "OAUTH2", null, 2, 11, 1);
        PlayerStatsEntity bob = new PlayerStatsEntity("Bob", "OAUTH2", null, 2, 9, 2);
        PlayerStatsEntity charlie = new PlayerStatsEntity("Charlie", "GUEST", null, 1, 2, 1);
        when(playerStatsRepository.findAll()).thenReturn(List.of(alice, bob, charlie));

        var leaderboard = gameService.getLeaderboard(2);
        assertEquals(2, leaderboard.size());
        assertEquals("Bob", leaderboard.get(0).playerName());
        assertEquals("Alice", leaderboard.get(1).playerName());
    }

    @Test
    public void testOAuth2RanksAboveGuestWithEqualStats() {
        PlayerStatsEntity oauthPlayer = new PlayerStatsEntity("OAuth2User", "OAUTH2", "u@x.com", 2, 10, 1);
        PlayerStatsEntity guestPlayer = new PlayerStatsEntity("GuestUser", "GUEST", null, 2, 10, 1);
        when(playerStatsRepository.findAll()).thenReturn(List.of(guestPlayer, oauthPlayer));

        var leaderboard = gameService.getLeaderboard();
        assertEquals("OAuth2User", leaderboard.get(0).playerName());
        assertEquals("GuestUser", leaderboard.get(1).playerName());
    }
}
