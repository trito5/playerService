package com.testgame.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Serdeable
public record StartBattleDto(@NonNull UUID battleId) {
}
