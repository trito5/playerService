package com.testgame.client.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record SaveScoreRequest(
    @NonNull UUID playerId,
    @Positive int score
) {
}
