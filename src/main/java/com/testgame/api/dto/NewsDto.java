package com.testgame.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;

@Serdeable
@Builder
public record NewsDto(
    @NonNull String message,
    @NonNull String type,
    @NonNull Instant timestamp
) {
}
