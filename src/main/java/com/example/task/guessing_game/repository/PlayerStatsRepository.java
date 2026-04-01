package com.example.task.guessing_game.repository;

import com.example.task.guessing_game.model.PlayerStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStatsEntity, Long> {
    Optional<PlayerStatsEntity> findByEmail(String email);
    Optional<PlayerStatsEntity> findByPlayerNameAndPlayerType(String playerName, String playerType);
    List<PlayerStatsEntity> findByPlayerTypeAndCreatedAtBefore(String playerType, LocalDateTime cutoff);
    List<PlayerStatsEntity> findAllByPlayerNameAndPlayerType(String playerName, String playerType);
}
