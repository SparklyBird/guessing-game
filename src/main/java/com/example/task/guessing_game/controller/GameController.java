package com.example.task.guessing_game.controller;

import com.example.task.guessing_game.model.GameState;
import com.example.task.guessing_game.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class GameController {
    private static final String GAME_STATE_SESSION_KEY = "gameState";
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
        System.out.println("GameController initialized!");
    }

    @GetMapping("/")
    public String showStartPage() {
        return "index";
    }

    @GetMapping("/name-entry")
    public String showNameEntryPage() {
        System.out.println("name-entry endpoint called!");
        return "name-entry";
    }

    @PostMapping("/start")
    public String startGame(@RequestParam String playerName, HttpSession session) {
        System.out.println("start endpoint called with playerName: " + playerName);
        String secretNumber = gameService.generateSecretNumber();
        GameState gameState = GameState.newGame(playerName, secretNumber);
        session.setAttribute(GAME_STATE_SESSION_KEY, gameState);
        return "redirect:/game";
    }

    // New endpoint for manual login (bypasses Spring Security authentication)
    @PostMapping("/manual-login")
    public String manualLogin(@RequestParam String playerName, HttpSession session) {
        System.out.println("manual-login endpoint called with playerName: " + playerName);
        String secretNumber = gameService.generateSecretNumber();
        GameState gameState = GameState.newGame(playerName, secretNumber);
        session.setAttribute(GAME_STATE_SESSION_KEY, gameState);
        return "redirect:/game";
    }

    // New endpoint for social login
    @GetMapping("/start-social")
    public String startSocialGame(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
        // Extract user details from OAuth2User
        Map<String, Object> attributes = principal.getAttributes();
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");

        // Use name or email as the player name
        String playerName = (name != null) ? name : email;

        System.out.println("start-social endpoint called with playerName: " + playerName);
        String secretNumber = gameService.generateSecretNumber();
        GameState gameState = GameState.newGame(playerName, secretNumber);
        session.setAttribute(GAME_STATE_SESSION_KEY, gameState);
        return "redirect:/game";
    }

    @GetMapping("/game")
    public String showGamePage(HttpSession session, Model model) {
        GameState gameState = (GameState) session.getAttribute(GAME_STATE_SESSION_KEY);
        model.addAttribute("gameState", gameState);
        return "game";
    }

    @PostMapping("/guess")
    public String handleGuess(@RequestParam String digit1, @RequestParam String digit2,
                              @RequestParam String digit3, @RequestParam String digit4,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        String guess = digit1 + digit2 + digit3 + digit4;
        GameState currentState = (GameState) session.getAttribute(GAME_STATE_SESSION_KEY);
        GameService.GuessResult result = gameService.checkGuess(currentState.secretNumber(), guess);

        int newTriesLeft = currentState.triesLeft() - 1;
        List<String> newHistory = new ArrayList<>(currentState.history());
        newHistory.add(0, "Guess: " + guess + " -> Result: " + result.toString());

        GameState nextState = new GameState(
                currentState.playerName(),
                currentState.secretNumber(),
                newTriesLeft,
                newHistory
        );

        session.setAttribute(GAME_STATE_SESSION_KEY, nextState);

        if (result.p() == 4) {
            redirectAttributes.addFlashAttribute("message", "You win!");
            return "redirect:/game-over";
        } else if (newTriesLeft <= 0) {
            redirectAttributes.addFlashAttribute("message", "You lose!");
            return "redirect:/game-over";
        }

        redirectAttributes.addFlashAttribute("lastResult", result.toString());
        return "redirect:/game";
    }

    @GetMapping("/game-over")
    public String showGameOverPage(HttpSession session, Model model) {
        GameState gameState = (GameState) session.getAttribute(GAME_STATE_SESSION_KEY);
        model.addAttribute("gameState", gameState);

        int guessesMade = 8 - gameState.triesLeft();
        boolean won = gameState.triesLeft() > 0;
        gameService.recordGameResult(gameState.playerName(), won, guessesMade, gameState.secretNumber());

        // Don't invalidate the session here, just remove the game state
        session.removeAttribute(GAME_STATE_SESSION_KEY);

        return "game-over";
    }
}