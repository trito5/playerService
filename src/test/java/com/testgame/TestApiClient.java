package com.testgame;

import com.testgame.api.dto.CompleteBattleDto;
import com.testgame.api.dto.PlayerStateDto;
import com.testgame.api.dto.StartBattleDto;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import java.util.UUID;
import lombok.Builder;

@Builder
public class TestApiClient {

    private static final String PLAYERS_PATH = "/players";
    private static final String BATTLES_PATH = "/battles";

    private final HttpClient client;

    public PlayerStateDto getPlayer(UUID playerId) {
        return getPlayer(playerId.toString());
    }

    public PlayerStateDto getPlayer(String playerId) {
        var request = HttpRequest.GET(PLAYERS_PATH + "/getPlayer/" + playerId);
        return client.toBlocking().retrieve(request, PlayerStateDto.class);
    }

    public StartBattleDto startBattle(UUID playerId, UUID levelId) {
        return startBattleResponse(playerId, levelId).getBody().orElseThrow();
    }

    public HttpResponse<StartBattleDto> startBattleResponse(UUID playerId, UUID levelId) {
        return startBattleResponse(playerId.toString(), levelId.toString());
    }

    public HttpResponse<StartBattleDto> startBattleResponse(String playerId, String levelId) {
        var path = BATTLES_PATH + "/startBattle/" + playerId + "/levelId/" + levelId;
        var request = HttpRequest.POST(path, null);
        return client.toBlocking().exchange(request, StartBattleDto.class);
    }

    public CompleteBattleDto completeBattle(UUID playerId, UUID battleId, int score) {
        return completeBattleResponse(playerId, battleId, score).getBody().orElseThrow();
    }

    public HttpResponse<CompleteBattleDto> completeBattleResponse(UUID playerId, UUID battleId, int score) {
        return completeBattleResponse(playerId.toString(), battleId.toString(), Integer.toString(score));
    }

    public HttpResponse<CompleteBattleDto> completeBattleResponse(
        String playerId, String battleId, String score
    ) {
        var path = BATTLES_PATH + "/completeBattle/" + playerId + "/battleId/" + battleId + "/score/" + score;
        var request = HttpRequest.POST(path, null);
        return client.toBlocking().exchange(request, CompleteBattleDto.class);
    }

}
