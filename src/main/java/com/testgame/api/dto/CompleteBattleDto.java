package com.testgame.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Builder
@Serdeable
public record CompleteBattleDto(int position) {
}
