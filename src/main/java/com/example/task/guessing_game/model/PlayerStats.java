package com.example.task.guessing_game.model;

public record PlayerStats(
        String playerName,
        String playerType,  // "GUEST" or "OAUTH2"
        int gamesPlayed,
        int totalGuesses,
        int wins
) implements Comparable<PlayerStats> {

    public double getSuccessRate() {
        if (gamesPlayed == 0) return 0.0;
        return (double) wins / gamesPlayed;
    }

    @Override
    public int compareTo(PlayerStats other) {
        // Primary: descending by wins
        int winsCompare = Integer.compare(other.wins(), this.wins());
        if (winsCompare != 0) return winsCompare;

        // Secondary: descending by success rate
        int rateCompare = Double.compare(other.getSuccessRate(), this.getSuccessRate());
        if (rateCompare != 0) return rateCompare;

        // Tertiary: ascending by total guesses
        int guessesCompare = Integer.compare(this.totalGuesses, other.totalGuesses);
        if (guessesCompare != 0) return guessesCompare;

        // OAUTH2 ranks above GUEST with equal stats
        boolean thisOAuth2 = "OAUTH2".equals(this.playerType);
        boolean otherOAuth2 = "OAUTH2".equals(other.playerType);
        if (thisOAuth2 && !otherOAuth2) return -1;
        if (!thisOAuth2 && otherOAuth2) return 1;
        return 0;
    }
}
