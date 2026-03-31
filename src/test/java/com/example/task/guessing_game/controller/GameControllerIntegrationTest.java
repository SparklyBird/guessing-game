package com.example.task.guessing_game.controller;

import com.example.task.guessing_game.model.GameState;
import com.example.task.guessing_game.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Test
    void getIndex_returnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getGame_withGameStateInSession_returnsGameView() throws Exception {
        GameState gameState = GameState.newGame("TestPlayer", "1234");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        mockMvc.perform(get("/game").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("game"))
                .andExpect(model().attributeExists("gameState"));
    }

    @Test
    void getGameOver_withWinningState_returnsGameOverView() throws Exception {
        // triesLeft = 5 means guessesMade = 3, won = true
        GameState gameState = new GameState("TestPlayer", "1234", 5, new ArrayList<>());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        doNothing().when(gameService).recordGameResult(anyString(), anyBoolean(), anyInt(), anyString());

        mockMvc.perform(get("/game-over").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("game-over"))
                .andExpect(model().attributeExists("gameState"));
    }

    @Test
    void getGameOver_withLosingState_returnsGameOverView() throws Exception {
        // triesLeft = 0 means guessesMade = 8, won = false
        GameState gameState = new GameState("TestPlayer", "1234", 0, new ArrayList<>());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        doNothing().when(gameService).recordGameResult(anyString(), anyBoolean(), anyInt(), anyString());

        mockMvc.perform(get("/game-over").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("game-over"));
    }

    @Test
    void postGuess_withCorrectGuess_redirectsToGameOver() throws Exception {
        GameState gameState = GameState.newGame("TestPlayer", "1234");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        when(gameService.checkGuess("1234", "1234")).thenReturn(new GameService.GuessResult(0, 4));

        mockMvc.perform(post("/guess")
                        .param("digit1", "1")
                        .param("digit2", "2")
                        .param("digit3", "3")
                        .param("digit4", "4")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/game-over"));
    }

    @Test
    void postGuess_withWrongGuessAndTriesRemaining_redirectsToGame() throws Exception {
        GameState gameState = GameState.newGame("TestPlayer", "1234");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        when(gameService.checkGuess("1234", "5678")).thenReturn(new GameService.GuessResult(0, 0));

        mockMvc.perform(post("/guess")
                        .param("digit1", "5")
                        .param("digit2", "6")
                        .param("digit3", "7")
                        .param("digit4", "8")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/game"));
    }

    @Test
    void postGuess_onLastTryWithWrongGuess_redirectsToGameOver() throws Exception {
        // triesLeft = 1: after decrement it becomes 0 → game over
        GameState gameState = new GameState("TestPlayer", "1234", 1, new ArrayList<>());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        when(gameService.checkGuess("1234", "5678")).thenReturn(new GameService.GuessResult(0, 0));

        mockMvc.perform(post("/guess")
                        .param("digit1", "5")
                        .param("digit2", "6")
                        .param("digit3", "7")
                        .param("digit4", "8")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/game-over"));
    }
}
