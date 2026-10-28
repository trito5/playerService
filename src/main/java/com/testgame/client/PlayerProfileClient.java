package com.testgame.client;

import com.testgame.client.dto.PlayerProfileResponse;
import reactor.core.publisher.Mono;

public interface PlayerProfileClient {

    Mono<PlayerProfileResponse> getPlayerDisplayName(String playerId);
}
