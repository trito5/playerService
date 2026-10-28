package com.testgame.controller;

import com.testgame.api.dto.PlayerStateDto;
import com.testgame.service.PlayerStateService;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Controller;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Controller("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerStateService playerStateService;

    @Get("/getPlayer/{playerId}")
    public Mono<PlayerStateDto> getPlayer(UUID playerId) {
        return playerStateService.getPlayer(playerId);
    }
}
