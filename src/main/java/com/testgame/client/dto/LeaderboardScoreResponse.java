package com.testgame.client.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record LeaderboardScoreResponse(@NonNull UUID playerId, int position) {
}
