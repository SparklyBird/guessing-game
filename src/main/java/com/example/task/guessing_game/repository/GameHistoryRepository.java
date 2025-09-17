package com.example.task.guessing_game.repository;

import com.example.task.guessing_game.model.GameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistoryEntity, Long> {
    List<GameHistoryEntity> findByPlayerName(String playerName);
    List<GameHistoryEntity> findByGameDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}