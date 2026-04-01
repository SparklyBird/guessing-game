package com.example.task.guessing_game.controller;

import com.example.task.guessing_game.model.GameState;
import com.example.task.guessing_game.model.PlayerStatsEntity;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
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
        GameState gameState = GameState.newGame("TestPlayer", 1L, "GUEST", "1234");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        mockMvc.perform(get("/game").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("game"))
                .andExpect(model().attributeExists("gameState"));
    }

    @Test
    void getGameOver_withWinningState_returnsGameOverView() throws Exception {
        GameState gameState = new GameState("TestPlayer", 1L, "GUEST", "1234", 5, new ArrayList<>());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        doNothing().when(gameService).recordGameResult(anyLong(), anyString(), anyBoolean(), anyInt(), anyString());

        mockMvc.perform(get("/game-over").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("game-over"))
                .andExpect(model().attributeExists("gameState"));
    }

    @Test
    void getGameOver_withLosingState_returnsGameOverView() throws Exception {
        GameState gameState = new GameState("TestPlayer", 1L, "GUEST", "1234", 0, new ArrayList<>());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gameState", gameState);

        doNothing().when(gameService).recordGameResult(anyLong(), anyString(), anyBoolean(), anyInt(), anyString());

        mockMvc.perform(get("/game-over").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("game-over"));
    }

    @Test
    void postGuess_withCorrectGuess_redirectsToGameOver() throws Exception {
        GameState gameState = GameState.newGame("TestPlayer", 1L, "GUEST", "1234");
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
        GameState gameState = GameState.newGame("TestPlayer", 1L, "GUEST", "1234");
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
        GameState gameState = new GameState("TestPlayer", 1L, "GUEST", "1234", 1, new ArrayList<>());
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

    @Test
    void getChooseName_withoutOAuth2Session_redirectsToHome() throws Exception {
        mockMvc.perform(get("/choose-name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void getChooseName_withOAuth2Session_returnsChooseNameView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth2Email", "user@example.com");
        session.setAttribute("oauth2SuggestedName", "Test User");

        mockMvc.perform(get("/choose-name").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("choose-name"))
                .andExpect(model().attribute("suggestedName", "Test User"));
    }

    @Test
    void postChooseName_withNameTakenByOAuth2_redirectsWithError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth2Email", "user@example.com");
        session.setAttribute("oauth2SuggestedName", "Taken");

        PlayerStatsEntity conflict = new PlayerStatsEntity("Taken", "OAUTH2", "other@example.com", 0, 0, 0);
        conflict.setId(99L);
        when(gameService.findOAuth2Conflict("Taken", "user@example.com")).thenReturn(Optional.of(conflict));

        mockMvc.perform(post("/choose-name")
                        .param("playerName", "Taken")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/choose-name"));
    }

    @Test
    void postChooseName_withAvailableName_startsGameAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth2Email", "user@example.com");
        session.setAttribute("oauth2SuggestedName", "NewName");

        when(gameService.findOAuth2Conflict("NewName", "user@example.com")).thenReturn(Optional.empty());
        doNothing().when(gameService).resolveGuestConflict("NewName");

        PlayerStatsEntity oauthPlayer = new PlayerStatsEntity("NewName", "OAUTH2", "user@example.com", 0, 0, 0);
        oauthPlayer.setId(5L);
        when(gameService.findOrCreateOAuth2Player("user@example.com")).thenReturn(oauthPlayer);
        when(gameService.assignNameToOAuth2Player(oauthPlayer, "NewName")).thenReturn(oauthPlayer);
        when(gameService.generateSecretNumber()).thenReturn("1234");

        mockMvc.perform(post("/choose-name")
                        .param("playerName", "NewName")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/game"));
    }

    @Test
    void postRestart_withValidPlayerStatsId_startsGameAndRedirects() throws Exception {
        PlayerStatsEntity player = new PlayerStatsEntity("ReturnUser", "OAUTH2", "ret@example.com", 3, 15, 2);
        player.setId(7L);
        when(gameService.getPlayerById(7L)).thenReturn(Optional.of(player));
        when(gameService.generateSecretNumber()).thenReturn("5678");

        mockMvc.perform(post("/restart").param("playerStatsId", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/game"));
    }

    @Test
    void postRestart_withInvalidId_redirectsToHome() throws Exception {
        when(gameService.getPlayerById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/restart").param("playerStatsId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
