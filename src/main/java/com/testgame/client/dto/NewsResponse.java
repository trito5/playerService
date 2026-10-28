package com.testgame.client.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record NewsResponse(
    @NonNull String message,
    @NonNull String type,
    @NonNull Instant timestamp
) {
}
