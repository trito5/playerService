package com.testgame.controller;

import com.testgame.api.dto.StartBattleDto;
import com.testgame.api.dto.CompleteBattleDto;
import com.testgame.service.BattleService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static io.micronaut.http.HttpStatus.CREATED;

@Controller("/battles")
@Validated
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    @Post("/completeBattle/{playerId}/battleId/{battleId}/score/{score}")
    public Mono<CompleteBattleDto> completeBattle(
        @PathVariable @NonNull UUID playerId,
        @PathVariable @NonNull UUID battleId,
        @PathVariable @Positive int score
    ) {
        return battleService.completeBattle(playerId, battleId, score);
    }

    @Post("/startBattle/{playerId}/levelId/{levelId}")
    @Status(CREATED)
    public Mono<StartBattleDto> startBattle(
        @PathVariable @NonNull UUID playerId,
        @PathVariable @NonNull UUID levelId) {
        return battleService.startBattle(playerId, levelId);
    }
}
