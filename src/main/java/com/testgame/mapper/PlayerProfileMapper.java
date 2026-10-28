package com.testgame.mapper;

import com.testgame.domain.PlayerName;
import com.testgame.client.dto.PlayerProfileResponse;
import java.util.UUID;
import lombok.NonNull;

public class PlayerProfileMapper { //TODO change name?

    public static PlayerName toPlayerName(UUID playerId, String name) {
        return PlayerName.builder()
            .playerId(playerId)
            .name(name)
            .build();
    }

    public static PlayerProfileResponse toMockedProfileResponse(@NonNull String playerId) {
        return PlayerProfileResponse.builder()
            .displayName("Player-" + playerId)
            .build();
    }
}
