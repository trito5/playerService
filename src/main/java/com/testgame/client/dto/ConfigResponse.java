package com.testgame.client.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record ConfigResponse(@NonNull List<LevelConfigResponse> levels) {
}
