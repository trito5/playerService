package com.testgame.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Builder;

@Builder
@Serdeable
@JsonInclude(JsonInclude.Include.ALWAYS)
public record LeaderboardDto(
    List<PositionDto> playerPosition,
    List<PositionDto> topTenPositions
) {
}
