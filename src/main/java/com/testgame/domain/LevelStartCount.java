package com.testgame.domain;

import io.micronaut.core.annotation.Introspected;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder
@Introspected
public record LevelStartCount(
    @NonNull UUID levelId,
    long startCount
) {
}
