package com.testgame.client.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record PlayerProfileResponse(@NonNull String displayName) {
}
