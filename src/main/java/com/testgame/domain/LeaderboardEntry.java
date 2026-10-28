package com.testgame.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record LeaderboardEntry(@NonNull UUID playerId, int score) {
}
