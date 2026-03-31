package com.example.task.guessing_game.model;

public record PlayerStats(
        String playerName,
        int gamesPlayed,
        int totalGuesses, // To rank players with the same success rate
        int wins
) implements Comparable<PlayerStats> {

    // Helper method to calculate the success rate
    public double getSuccessRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) wins / gamesPlayed;
    }

    // Sorting logic for the leaderboard
    @Override
    public int compareTo(PlayerStats other) {
        // Primary sorting: descending by number of wins
        int winsCompare = Integer.compare(other.wins(), this.wins());
        if (winsCompare != 0) {
            return winsCompare;
        }

        // Secondary sorting: descending by success rate
        int rateCompare = Double.compare(other.getSuccessRate(), this.getSuccessRate());
        if (rateCompare != 0) {
            return rateCompare;
        }

        // Tertiary sorting: ascending by total guesses (fewer is better)
        return Integer.compare(this.totalGuesses, other.totalGuesses);
    }
}