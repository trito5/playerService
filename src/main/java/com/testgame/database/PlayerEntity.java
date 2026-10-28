package com.testgame.database;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
@MappedEntity("players")
public record PlayerEntity(
    @NonNull @Id UUID playerId,
    @NonNull Instant activity
) {
}
