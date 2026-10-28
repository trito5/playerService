package com.testgame.database;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Mono;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface PlayerRepository extends ReactorCrudRepository<PlayerEntity, UUID> {

    @Query("""
        INSERT INTO players (player_id, activity)
        VALUES (:playerId, :activity)
        ON CONFLICT (player_id) DO UPDATE SET activity = EXCLUDED.activity
        """)
    Mono<Long> upsertPlayer(UUID playerId, Instant activity);

}
