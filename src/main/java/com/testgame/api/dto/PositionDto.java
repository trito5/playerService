package com.testgame.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.NonNull;
import java.util.UUID;

@Serdeable
@Builder
public record PositionDto(
    @NonNull UUID playerId,
    @NonNull String playerName,
    int position,
    int score
) {
}
