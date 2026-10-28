package com.testgame.mapper;

import com.testgame.database.BattleEntity;
import com.testgame.domain.LevelStartCount;
import com.testgame.domain.PlayerDailyScore;
import com.testgame.api.dto.CompleteBattleDto;
import com.testgame.api.dto.StartBattleDto;
import com.testgame.client.dto.LeaderboardScoreResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BattleMapper {

    public static BattleEntity toCompletedBattle(BattleEntity battle, int score) {
        Instant now = Instant.now();
        return battle.toBuilder()
            .score(score)
            .completedAt(now)
            .updatedAt(now)
            .build();
    }

    public static CompleteBattleDto toCompleteBattleDto(LeaderboardScoreResponse response) {
        return CompleteBattleDto.builder()
            .position(response.position())
            .build();
    }

    public static PlayerDailyScore toPlayerDailyScore(
        UUID playerId, LocalDate day, int score
    ) {
        return PlayerDailyScore.builder()
            .playerId(playerId)
            .day(day)
            .score(score)
            .build();
    }

    public static BattleEntity toNewBattle(UUID playerId, UUID levelId, Instant startedAt) {
        return BattleEntity.builder()
            .battleId(UUID.randomUUID())
            .playerId(playerId)
            .levelId(levelId)
            .score(0)
            .startedAt(startedAt)
            .updatedAt(startedAt)
            .build();
    }

    public static StartBattleDto toStartBattleDto(BattleEntity battle) {
        return StartBattleDto.builder()
            .battleId(battle.battleId())
            .build();
    }

    public static LevelStartCount toLevelStartCount(UUID levelId, long startCount) {
        return LevelStartCount.builder()
            .levelId(levelId)
            .startCount(startCount)
            .build();
    }

    public static List<LevelStartCount> toLevelStartCounts(Map<UUID, Long> counts) {
        return counts.keySet().stream()
            .map(levelId -> toLevelStartCount(levelId, counts.get(levelId)))
            .toList();
    }
}
