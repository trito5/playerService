package com.testgame.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record PlayerStateDto(
    @NonNull List<LevelDto> levels,
    @NonNull LeaderboardDto leaderboardDto,
    @NonNull List<NewsDto> news,
    @NonNull Instant activity
) {
}
