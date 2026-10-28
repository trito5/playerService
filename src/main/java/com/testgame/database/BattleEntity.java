package com.testgame.database;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder(toBuilder = true)
@MappedEntity("battles")
public record BattleEntity(
    @NonNull @Id UUID battleId,
    @NonNull UUID playerId,
    @NonNull UUID levelId,
    int score,
    @NonNull Instant startedAt,
    @NonNull Instant updatedAt,
    @Nullable Instant completedAt
) {
}
