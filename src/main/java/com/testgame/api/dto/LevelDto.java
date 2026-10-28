package com.testgame.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.NonNull;
import java.util.UUID;

@Serdeable
@Builder
public record LevelDto(
    @NonNull UUID levelId,
    @NonNull String levelName,
    int startCount
) {
}
