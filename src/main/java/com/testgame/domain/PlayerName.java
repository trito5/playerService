package com.testgame.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record PlayerName(
    @NonNull UUID playerId,
    @NonNull String name
) {
}
