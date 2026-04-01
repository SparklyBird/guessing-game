package com.example.task.guessing_game.service;

import com.example.task.guessing_game.model.PlayerStatsEntity;
import com.example.task.guessing_game.repository.GameHistoryRepository;
import com.example.task.guessing_game.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GuestCleanupService {

    private final PlayerStatsRepository playerStatsRepository;
    private final GameHistoryRepository gameHistoryRepository;

    @Autowired
    public GuestCleanupService(PlayerStatsRepository playerStatsRepository,
                               GameHistoryRepository gameHistoryRepository) {
        this.playerStatsRepository = playerStatsRepository;
        this.gameHistoryRepository = gameHistoryRepository;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000L)
    @Transactional
    public void deleteExpiredGuests() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        List<PlayerStatsEntity> expiredGuests =
                playerStatsRepository.findByPlayerTypeAndCreatedAtBefore("GUEST", cutoff);
        for (PlayerStatsEntity guest : expiredGuests) {
            gameHistoryRepository.deleteByPlayerStatsId(guest.getId());
            playerStatsRepository.delete(guest);
        }
    }
}
