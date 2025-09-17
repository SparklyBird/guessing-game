package com.example.task.guessing_game.service;

import com.example.task.guessing_game.model.PlayerStatsEntity;
import com.example.task.guessing_game.repository.GameHistoryRepository;
import com.example.task.guessing_game.repository.PlayerStatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

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
        // Check all digits are unique
        assertEquals(4, secretNumber.chars().distinct().count());
    }

    @Test
    public void testCheckGuess() {
        // Test case 1: No matches
        GameService.GuessResult result1 = gameService.checkGuess("1234", "5678");
        assertEquals(0, result1.m());
        assertEquals(0, result1.p());
        // Test case 2: One digit in correct position
        GameService.GuessResult result2 = gameService.checkGuess("1234", "1567");
        assertEquals(0, result2.m());
        assertEquals(1, result2.p());
        // Test case 3: One digit in wrong position
        GameService.GuessResult result3 = gameService.checkGuess("1234", "5123");
        assertEquals(3, result3.m());
        assertEquals(0, result3.p());
        // Test case 4: Two digits in correct position, one in wrong
        GameService.GuessResult result4 = gameService.checkGuess("1234", "1243");
        assertEquals(2, result4.m());
        assertEquals(2, result4.p());
        // Test case 5: All correct
        GameService.GuessResult result5 = gameService.checkGuess("1234", "1234");
        assertEquals(0, result5.m());
        assertEquals(4, result5.p());
    }

    @Test
    public void testRecordGameResult() {
        // Test recording a new player's win
        when(playerStatsRepository.findById("Alice")).thenReturn(Optional.empty());
        when(playerStatsRepository.save(any(PlayerStatsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        gameService.recordGameResult("Alice", true, 3, "1234");
        // Verify that the repositories were called
        verify(gameHistoryRepository, times(1)).save(any());
        verify(playerStatsRepository, times(1)).save(any());
        // Reset mocks for the next test
        reset(playerStatsRepository, gameHistoryRepository);
        // Test recording an existing player's loss
        PlayerStatsEntity existingPlayer = new PlayerStatsEntity("Bob", 2, 10, 1);
        when(playerStatsRepository.findById("Bob")).thenReturn(Optional.of(existingPlayer));
        when(playerStatsRepository.save(any(PlayerStatsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        gameService.recordGameResult("Bob", false, 8, "5678");
        // Verify that the repositories were called again
        verify(gameHistoryRepository, times(1)).save(any());
        verify(playerStatsRepository, times(1)).save(any());
    }

    @Test
    public void testLeaderboard() {
        // Create mock player stats
        PlayerStatsEntity alice = new PlayerStatsEntity("Alice", 2, 11, 1);
        PlayerStatsEntity bob = new PlayerStatsEntity("Bob", 2, 9, 2);
        PlayerStatsEntity charlie = new PlayerStatsEntity("Charlie", 2, 10, 1);
        // Mock repository to return these stats
        when(playerStatsRepository.findAll()).thenReturn(List.of(alice, bob, charlie));
        var leaderboard = gameService.getLeaderboard();
        // Check the leaderboard is sorted correctly
        // Bob: 2 wins, 0 losses (100% success rate), 9 total guesses
        // Charlie: 1 win, 1 loss (50% success rate), 10 total guesses
        // Alice: 1 win, 1 loss (50% success rate), 11 total guesses
        assertEquals("Bob", leaderboard.get(0).playerName());
        // Charlie and Alice both have 50% success rate, but Charlie has fewer total guesses
        assertEquals("Charlie", leaderboard.get(1).playerName());
        assertEquals("Alice", leaderboard.get(2).playerName());
    }

    @Test
    public void testLeaderboardWithMinGamesFilter() {
        // Create mock player stats
        PlayerStatsEntity alice = new PlayerStatsEntity("Alice", 2, 11, 1);
        PlayerStatsEntity bob = new PlayerStatsEntity("Bob", 2, 9, 2);
        PlayerStatsEntity charlie = new PlayerStatsEntity("Charlie", 1, 2, 1);
        // Mock repository to return these stats
        when(playerStatsRepository.findAll()).thenReturn(List.of(alice, bob, charlie));
        var leaderboard = gameService.getLeaderboard(2);
        // Only Alice and Bob should be in the leaderboard (min 2 games)
        assertEquals(2, leaderboard.size());
        assertEquals("Bob", leaderboard.get(0).playerName());  // Bob has 100% success rate
        assertEquals("Alice", leaderboard.get(1).playerName()); // Alice has 50% success rate
    }
}