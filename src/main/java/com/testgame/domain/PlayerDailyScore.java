package com.testgame.domain;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record PlayerDailyScore(
    @NonNull UUID playerId,
    @NonNull LocalDate day,
    int score
) {
}
