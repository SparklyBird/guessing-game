package com.example.task.guessing_game.controller;

import com.example.task.guessing_game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LeaderboardController {
    private final GameService gameService;
    @Autowired
    public LeaderboardController(GameService gameService) {
        this.gameService = gameService;
    }
    @GetMapping("/leaderboard")
    public String showLeaderboard(@RequestParam(defaultValue = "1") int minGames,
                                  Model model,
                                  @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("leaderboard", gameService.getLeaderboard(minGames));
        model.addAttribute("minGames", minGames);
        // Add user information if authenticated
        if (principal != null) {
            model.addAttribute("user", principal.getAttributes());
        }
        return "leaderboard";
    }
}