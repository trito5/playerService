package com.testgame.client;

import com.testgame.client.dto.ConfigResponse;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

import static com.testgame.mapper.ConfigMapper.toConfigResponse;
import static com.testgame.mapper.ConfigMapper.toLevelConfigResponse;

@Singleton
@Requires(property = "config.mock.enabled", value = "true")
public class MockConfigClient implements ConfigClient {

    private static final ConfigResponse CONFIG = toConfigResponse(List.of(
        toLevelConfigResponse(UUID.fromString("10000000-0000-0000-0000-000000000001"), "Level 1", 3),
        toLevelConfigResponse(UUID.fromString("10000000-0000-0000-0000-000000000002"), "Level 2", 3),
        toLevelConfigResponse(UUID.fromString("10000000-0000-0000-0000-000000000003"), "Level 3", 3)
    ));

    @Override
    public Mono<ConfigResponse> getConfig() {
        return Mono.just(CONFIG);
    }
}
