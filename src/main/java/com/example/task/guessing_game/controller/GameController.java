package com.example.task.guessing_game.controller;

import com.example.task.guessing_game.model.GameState;
import com.example.task.guessing_game.model.PlayerStatsEntity;
import com.example.task.guessing_game.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GameController {
    private static final String GAME_STATE_KEY = "gameState";
    private static final String OAUTH2_EMAIL_KEY = "oauth2Email";
    private static final String OAUTH2_SUGGESTED_NAME_KEY = "oauth2SuggestedName";

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String showStartPage() {
        return "index";
    }

    @GetMapping("/name-entry")
    public String showNameEntryPage() {
        return "name-entry";
    }

    /**
     * Returns whether a GUEST with the given name already exists.
     * Used for client-side pre-validation if needed.
     */
    @GetMapping("/check-name")
    @ResponseBody
    public Map<String, Boolean> checkName(@RequestParam String playerName) {
        return Map.of("taken", gameService.guestNameExists(playerName.trim()));
    }

    /**
     * Guest login. If a guest with the same name already exists and force=false,
     * redirects back to name-entry with a warning. If force=true, overwrites old guest.
     */
    @PostMapping("/start")
    public String startGame(@RequestParam String playerName,
                            @RequestParam(defaultValue = "false") boolean force,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        String trimmedName = playerName.trim();

        // Block completely if an OAuth2 user owns this name
        if (gameService.oauth2NameExists(trimmedName)) {
            redirectAttributes.addFlashAttribute("error",
                    "That name is taken by a registered Google account. Please choose a different name.");
            redirectAttributes.addFlashAttribute("takenName", trimmedName);
            return "redirect:/name-entry";
        }

        // Warn if a guest owns this name (but allow force override)
        if (!force && gameService.guestNameExists(trimmedName)) {
            redirectAttributes.addFlashAttribute("warning",
                    "A guest with that name already exists. Choose a different name or continue anyway.");
            redirectAttributes.addFlashAttribute("takenName", trimmedName);
            return "redirect:/name-entry";
        }

        PlayerStatsEntity guest = gameService.createGuestPlayer(trimmedName);
        String secretNumber = gameService.generateSecretNumber();
        GameState gameState = GameState.newGame(guest.getPlayerName(), guest.getId(), "GUEST", secretNumber);
        session.setAttribute(GAME_STATE_KEY, gameState);
        return "redirect:/game";
    }

    /**
     * OAuth2 success handler.
     * Returning users (name already set, not equal to email placeholder) skip /choose-name.
     * New users go to /choose-name to pick a display name.
     */
    @GetMapping("/start-social")
    public String startSocialGame(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
        Map<String, Object> attrs = principal.getAttributes();
        String email = (String) attrs.get("email");
        String suggestedName = (String) attrs.getOrDefault("name", email);

        PlayerStatsEntity existing = gameService.findOrCreateOAuth2Player(email);
        if (existing.getPlayerName() != null && !existing.getPlayerName().equals(email)) {
            // Returning user — start game directly
            String secretNumber = gameService.generateSecretNumber();
            GameState gameState = GameState.newGame(existing.getPlayerName(), existing.getId(), "OAUTH2", secretNumber);
            session.setAttribute(GAME_STATE_KEY, gameState);
            return "redirect:/game";
        }

        // New user — choose a display name
        session.setAttribute(OAUTH2_EMAIL_KEY, email);
        session.setAttribute(OAUTH2_SUGGESTED_NAME_KEY, suggestedName);
        return "redirect:/choose-name";
    }

    @GetMapping("/choose-name")
    public String showChooseNamePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute(OAUTH2_EMAIL_KEY);
        if (email == null) return "redirect:/";
        model.addAttribute("suggestedName", session.getAttribute(OAUTH2_SUGGESTED_NAME_KEY));
        return "choose-name";
    }

    @PostMapping("/choose-name")
    public String handleChooseName(@RequestParam String playerName, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute(OAUTH2_EMAIL_KEY);
        if (email == null) return "redirect:/";

        String chosenName = playerName.trim();

        Optional<PlayerStatsEntity> oauth2Conflict = gameService.findOAuth2Conflict(chosenName, email);
        if (oauth2Conflict.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "That name is already taken. Please choose another.");
            redirectAttributes.addFlashAttribute("suggestedName", chosenName);
            return "redirect:/choose-name";
        }

        gameService.resolveGuestConflict(chosenName);

        PlayerStatsEntity oauthPlayer = gameService.findOrCreateOAuth2Player(email);
        oauthPlayer = gameService.assignNameToOAuth2Player(oauthPlayer, chosenName);

        session.removeAttribute(OAUTH2_EMAIL_KEY);
        session.removeAttribute(OAUTH2_SUGGESTED_NAME_KEY);

        String secretNumber = gameService.generateSecretNumber();
        GameState gameState = GameState.newGame(oauthPlayer.getPlayerName(), oauthPlayer.getId(), "OAUTH2", secretNumber);
        session.setAttribute(GAME_STATE_KEY, gameState);
        return "redirect:/game";
    }

    /** Restart: look up the existing player record by ID and start a new game (works for both GUEST and OAUTH2). */
    @PostMapping("/restart")
    public String restartGame(@RequestParam Long playerStatsId, HttpSession session) {
        return gameService.getPlayerById(playerStatsId)
                .map(player -> {
                    String secretNumber = gameService.generateSecretNumber();
                    GameState gameState = GameState.newGame(
                            player.getPlayerName(), player.getId(), player.getPlayerType(), secretNumber);
                    session.setAttribute(GAME_STATE_KEY, gameState);
                    return "redirect:/game";
                })
                .orElse("redirect:/");
    }

    @GetMapping("/game")
    public String showGamePage(HttpSession session, Model model) {
        GameState gameState = (GameState) session.getAttribute(GAME_STATE_KEY);
        model.addAttribute("gameState", gameState);
        return "game";
    }

    @PostMapping("/guess")
    public String handleGuess(@RequestParam String digit1, @RequestParam String digit2,
                              @RequestParam String digit3, @RequestParam String digit4,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        String guess = digit1 + digit2 + digit3 + digit4;
        GameState currentState = (GameState) session.getAttribute(GAME_STATE_KEY);
        GameService.GuessResult result = gameService.checkGuess(currentState.secretNumber(), guess);

        int newTriesLeft = currentState.triesLeft() - 1;
        List<String> newHistory = new ArrayList<>(currentState.history());
        newHistory.add(0, "Guess: " + guess + " -> Result: " + result.toString());

        GameState nextState = new GameState(
                currentState.playerName(),
                currentState.playerStatsId(),
                currentState.playerType(),
                currentState.secretNumber(),
                newTriesLeft,
                newHistory
        );
        session.setAttribute(GAME_STATE_KEY, nextState);

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
        GameState gameState = (GameState) session.getAttribute(GAME_STATE_KEY);
        model.addAttribute("gameState", gameState);

        int guessesMade = 8 - gameState.triesLeft();
        boolean won = gameState.triesLeft() > 0;
        gameService.recordGameResult(
                gameState.playerStatsId(), gameState.playerName(), won, guessesMade, gameState.secretNumber());

        session.removeAttribute(GAME_STATE_KEY);
        return "game-over";
    }
}
