package com.testgame.mapper;

import com.testgame.client.dto.ConfigResponse;
import com.testgame.client.dto.LevelConfigResponse;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;

public class ConfigMapper {

    public static ConfigResponse toConfigResponse(@NonNull List<LevelConfigResponse> levels) {
        return ConfigResponse.builder()
            .levels(levels)
            .build();
    }

    public static LevelConfigResponse toLevelConfigResponse(
        @NonNull UUID levelId,
        @NonNull String levelName,
        int maxAttemptsPerDay
    ) {
        return LevelConfigResponse.builder()
            .levelId(levelId)
            .levelName(levelName)
            .maxAttemptsPerDay(maxAttemptsPerDay)
            .build();
    }
}
