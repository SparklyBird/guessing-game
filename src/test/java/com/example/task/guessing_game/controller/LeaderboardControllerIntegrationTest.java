package com.example.task.guessing_game.controller;

import com.example.task.guessing_game.model.PlayerStats;
import com.example.task.guessing_game.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaderboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Test
    void getLeaderboard_unauthenticated_returnsLeaderboardView() throws Exception {
        when(gameService.getLeaderboard(1)).thenReturn(List.of());

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(model().attributeExists("leaderboard"))
                .andExpect(model().attribute("minGames", 1));
    }

    @Test
    void getLeaderboard_withMinGamesParam_passesFilterToService() throws Exception {
        PlayerStats alice = new PlayerStats("Alice", 3, 18, 2);
        when(gameService.getLeaderboard(3)).thenReturn(List.of(alice));

        mockMvc.perform(get("/leaderboard").param("minGames", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(model().attribute("minGames", 3))
                .andExpect(model().attribute("leaderboard", List.of(alice)));
    }

    @Test
    void getLeaderboard_withPopulatedData_exposesPlayersInModel() throws Exception {
        List<PlayerStats> players = List.of(
                new PlayerStats("Bob", 5, 25, 4),
                new PlayerStats("Alice", 3, 18, 2)
        );
        when(gameService.getLeaderboard(1)).thenReturn(players);

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("leaderboard", players));
    }

    @Test
    @WithMockUser
    void getLeaderboard_withNonOAuth2User_doesNotAddUserAttribute() throws Exception {
        // @WithMockUser provides a UserDetails-based principal, not an OAuth2User.
        // The controller's principal == null guard means no "user" attribute is set.
        when(gameService.getLeaderboard(1)).thenReturn(List.of());

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(model().attributeDoesNotExist("user"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLeaderboard_authenticatedUser_canAccessLeaderboard() throws Exception {
        when(gameService.getLeaderboard(1)).thenReturn(List.of());

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"));
    }
}
