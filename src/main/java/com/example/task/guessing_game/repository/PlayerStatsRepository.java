package com.example.task.guessing_game.repository;

import com.example.task.guessing_game.model.PlayerStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStatsEntity, String> {
}