package com.testgame.database;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface BattleRepository extends ReactorCrudRepository<BattleEntity, UUID> {

    Flux<BattleEntity> findByPlayerIdAndStartedAtGreaterThanEqualsAndStartedAtLessThan(
        UUID playerId, Instant dayStart, Instant nextDayStart
    );

    Flux<BattleEntity> findByPlayerIdAndCompletedAtGreaterThanEqualsAndCompletedAtLessThan(
        UUID playerId, Instant dayStart, Instant nextDayStart
    );

    default Mono<Long> completeBattle(BattleEntity battle) {
        return completeBattle(
            battle.battleId(),
            battle.playerId(),
            battle.score(),
            battle.completedAt()
        );
    }

    @Query("""
        UPDATE battles
        SET score = :score, completed_at = :completedAt, updated_at = :completedAt
        WHERE battle_id = :battleId AND player_id = :playerId
            AND completed_at IS NULL
        """)
    Mono<Long> completeBattle(UUID battleId, UUID playerId, int score, Instant completedAt);

    Mono<Long> deleteByPlayerIdIn(Collection<UUID> playerIds);
}
