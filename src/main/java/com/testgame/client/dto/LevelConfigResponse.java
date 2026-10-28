package com.testgame.client.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record LevelConfigResponse(
    @NonNull UUID levelId,
    @NonNull String levelName,
    int maxAttemptsPerDay
) {
}
