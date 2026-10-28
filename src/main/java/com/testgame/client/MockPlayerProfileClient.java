package com.testgame.client;

import com.testgame.client.dto.PlayerProfileResponse;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import lombok.NonNull;
import reactor.core.publisher.Mono;
import static com.testgame.mapper.PlayerProfileMapper.toMockedProfileResponse;

@Singleton
@Requires(property = "player-profile.mock.enabled", value = "true")
public class MockPlayerProfileClient implements PlayerProfileClient {

    @Override
    public Mono<PlayerProfileResponse> getPlayerDisplayName(@NonNull String playerId) {
        return Mono.just(toMockedProfileResponse(playerId));
    }
}
