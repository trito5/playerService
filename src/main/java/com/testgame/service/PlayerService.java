package com.testgame.service;

import com.testgame.client.PlayerProfileClient;
import com.testgame.client.dto.PlayerProfileResponse;
import com.testgame.database.PlayerEntity;
import com.testgame.database.PlayerRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Singleton
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerProfileClient playerProfileClient;

    public Mono<String> getPlayerDisplayName(UUID playerId) {
        return playerProfileClient.getPlayerDisplayName(playerId.toString())
            .map(PlayerProfileResponse::displayName)
            .filter(name -> !name.isBlank())
            .switchIfEmpty(Mono.error(new HttpStatusException(
                HttpStatus.NOT_FOUND, "Display name not found for player " + playerId
            )));
    }

    public Mono<PlayerEntity> recordPlayerActivity(UUID playerId) {
        return playerRepository.upsertPlayer(playerId, Instant.now())
            .then(playerRepository.findById(playerId));
    }
}
