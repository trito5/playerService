package com.testgame.service;

import com.testgame.client.ConfigClient;
import com.testgame.client.dto.ConfigResponse;
import com.testgame.client.dto.LevelConfigResponse;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static io.micronaut.http.HttpStatus.NOT_FOUND;

@Singleton
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigClient configClient;

    public Mono<ConfigResponse> getConfig() {
        return configClient.getConfig();
    }

    public Mono<LevelConfigResponse> getLevel(UUID levelId) {
        return getConfig()
            .flatMapMany(config -> Flux.fromIterable(config.levels()))
            .filter(level -> level.levelId().equals(levelId))
            .next()
            .switchIfEmpty(Mono.error(new HttpStatusException(NOT_FOUND, "Level not found: " + levelId)));
    }

}
