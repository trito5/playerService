package com.testgame.service;

import com.testgame.client.dto.LevelConfigResponse;
import com.testgame.database.BattleEntity;
import com.testgame.database.BattleRepository;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static com.testgame.mapper.BattleMapper.toNewBattle;
import static com.testgame.util.DateTimeUtil.startOfDay;
import static com.testgame.util.DateTimeUtil.startOfNextDay;
import static com.testgame.util.DateTimeUtil.toUtcDate;
import static io.micronaut.http.HttpStatus.CONFLICT;
import static io.micronaut.transaction.TransactionDefinition.Isolation.READ_COMMITTED;

@Singleton
@RequiredArgsConstructor
public class BattleStartService {

    private final BattleRepository battleRepository;
    private final PlayerService playerService;

    @Transactional(isolation = READ_COMMITTED)
    public Mono<BattleEntity> startBattle(UUID playerId, LevelConfigResponse level) {
        // The activity upsert holds the player row lock until this transaction completes.
        return playerService.recordPlayerActivity(playerId)
            .flatMap(_ -> createBattleIfAllowed(playerId, level));
    }

    private Mono<BattleEntity> createBattleIfAllowed(UUID playerId, LevelConfigResponse level) {
        var startedAt = Instant.now();
        var day = toUtcDate(startedAt);
        return countStartsForLevel(playerId, level.levelId(), day)
            .flatMap(count -> saveNewBattle(playerId, level, count, startedAt));
    }

    private Mono<Long> countStartsForLevel(UUID playerId, UUID levelId, LocalDate day) {
        return battleRepository.findByPlayerIdAndStartedAtGreaterThanEqualsAndStartedAtLessThan(
                playerId, startOfDay(day), startOfNextDay(day))
            .filter(battle -> battle.levelId().equals(levelId))
            .count();
    }

    private Mono<BattleEntity> saveNewBattle(
        UUID playerId,
        LevelConfigResponse level,
        long startCount,
        Instant startedAt
    ) {
        if (startCount >= level.maxAttemptsPerDay()) {
            return Mono.error(new HttpStatusException(
                CONFLICT, "Daily battle limit reached for level " + level.levelId()));
        }
        return battleRepository.save(toNewBattle(playerId, level.levelId(), startedAt));
    }
}
