package com.testgame.service;

import com.testgame.database.BattleEntity;
import com.testgame.database.BattleRepository;
import com.testgame.domain.LevelStartCount;
import com.testgame.domain.PlayerDailyScore;
import com.testgame.api.dto.CompleteBattleDto;
import com.testgame.api.dto.StartBattleDto;
import com.testgame.client.dto.LeaderboardScoreResponse;
import com.testgame.mapper.BattleMapper;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static com.testgame.mapper.BattleMapper.toCompletedBattle;
import static com.testgame.util.DateTimeUtil.startOfDay;
import static com.testgame.util.DateTimeUtil.startOfNextDay;
import static com.testgame.util.DateTimeUtil.toUtcDate;
import static com.testgame.util.DateTimeUtil.todayUtc;
import static io.micronaut.http.HttpStatus.CONFLICT;
import static io.micronaut.http.HttpStatus.NOT_FOUND;

@Singleton
@RequiredArgsConstructor
public class BattleService {

    private final BattleRepository battleRepository;
    private final PlayerService playerService;
    private final ConfigService configService;
    private final LeaderboardService leaderboardService;
    private final BattleStartService battleStartService;

    public Flux<LevelStartCount> countStartsByLevelToday(UUID playerId) {
        var today = todayUtc();
        return getStartedBattlesForDay(playerId, today)
            .collect(HashMap<UUID, Long>::new, this::countLevelStart)
            .flatMapIterable(BattleMapper::toLevelStartCounts);
    }

    public Mono<Integer> getScoreForToday(UUID playerId) {
        return getDailyScore(playerId, todayUtc())
            .map(PlayerDailyScore::score);
    }

    public Mono<StartBattleDto> startBattle(UUID playerId, UUID levelId) {
        return configService.getLevel(levelId)
            .flatMap(level -> battleStartService.startBattle(playerId, level))
            .map(BattleMapper::toStartBattleDto);
    }

    public Mono<CompleteBattleDto> completeBattle(UUID playerId, UUID battleId, int score) {
        return getPlayerBattle(playerId, battleId)
            .flatMap(battle -> saveCompletedBattle(battle, score))
            .flatMap(this::updatePlayerAndLeaderboard)
            .map(BattleMapper::toCompleteBattleDto);
    }

    private Mono<BattleEntity> getPlayerBattle(UUID playerId, UUID battleId) {
        return battleRepository.findById(battleId)
            .filter(battle -> battle.playerId().equals(playerId))
            .switchIfEmpty(Mono.error(new HttpStatusException(NOT_FOUND, "Battle not found: " + battleId)));
    }

    private Mono<BattleEntity> saveCompletedBattle(BattleEntity battle, int score) {
        var completedBattle = toCompletedBattle(battle, score);
        return battleRepository.completeBattle(completedBattle)
            .filter(updatedRows -> updatedRows == 1)
            .map(updatedRows -> completedBattle)
            .switchIfEmpty(Mono.error(new HttpStatusException(CONFLICT, "Battle has already been completed")));
    }

    private Mono<LeaderboardScoreResponse> updatePlayerAndLeaderboard(BattleEntity battle) {
        var day = toUtcDate(battle.completedAt());
        return playerService.recordPlayerActivity(battle.playerId())
            .then(getDailyScore(battle.playerId(), day))
            .flatMap(leaderboardService::updatePlayerScore);
    }

    private Mono<PlayerDailyScore> getDailyScore(UUID playerId, LocalDate day) {
        return getCompletedBattlesForDay(playerId, day)
            .map(BattleEntity::score)
            .reduce(0, Math::addExact)
            .map(score -> BattleMapper.toPlayerDailyScore(playerId, day, score));
    }

    private void countLevelStart(Map<UUID, Long> counts, BattleEntity battle) {
        UUID levelId = battle.levelId();
        long previousCount = counts.getOrDefault(levelId, 0L);
        counts.put(levelId, previousCount + 1);
    }

    private Flux<BattleEntity> getStartedBattlesForDay(UUID playerId, LocalDate day) {
        var dayStart = startOfDay(day);
        var nextDayStart = startOfNextDay(day);

        return battleRepository.findByPlayerIdAndStartedAtGreaterThanEqualsAndStartedAtLessThan(
            playerId, dayStart, nextDayStart);
    }

    private Flux<BattleEntity> getCompletedBattlesForDay(UUID playerId, LocalDate day) {
        var dayStart = startOfDay(day);
        var nextDayStart = startOfNextDay(day);

        return battleRepository.findByPlayerIdAndCompletedAtGreaterThanEqualsAndCompletedAtLessThan(
            playerId, dayStart, nextDayStart);
    }

}
