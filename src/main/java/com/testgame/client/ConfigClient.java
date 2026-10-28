package com.testgame.client;

import com.testgame.client.dto.ConfigResponse;
import reactor.core.publisher.Mono;

public interface ConfigClient {

    Mono<ConfigResponse> getConfig();
}
