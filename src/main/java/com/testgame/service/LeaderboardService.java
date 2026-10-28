package com.testgame.service;

import com.testgame.client.LeaderboardClient;
import com.testgame.domain.PlayerName;
import com.testgame.domain.PlayerDailyScore;
import com.testgame.api.dto.LeaderboardDto;
import com.testgame.client.dto.LeaderboardResponse;
import com.testgame.client.dto.LeaderboardScoreResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.testgame.mapper.LeaderboardMapper.toLeaderboardDto;
import static com.testgame.mapper.LeaderboardMapper.toSaveScoreRequest;
import static com.testgame.mapper.PlayerProfileMapper.toPlayerName;

@Singleton
@RequiredArgsConstructor
public class LeaderboardService {

    private static final int TOP_LIST_SIZE = 10;
    private static final int NEIGHBOURS_PER_SIDE = 2;

    private final LeaderboardClient leaderboardClient;
    private final PlayerService playerService;

    public Mono<LeaderboardScoreResponse> updatePlayerScore(PlayerDailyScore score) {
        return leaderboardClient.updatePlayerScore(toSaveScoreRequest(score))
            .switchIfEmpty(Mono.error(new HttpStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Leaderboard Service returned no position"
            )));
    }

    public Mono<LeaderboardDto> getLeaderboard(UUID playerId, int dailyScore) {
        return getPlayerLeaderboard(playerId, dailyScore)
            .flatMap(neighbours -> leaderboardClient.getLeaderboard(1, TOP_LIST_SIZE)
                .flatMap(topTen -> addNamesToLeaderboard(neighbours, topTen)));
    }

    private Mono<List<LeaderboardResponse>> getPlayerLeaderboard(UUID playerId, int dailyScore) {
        return getPlayerPosition(playerId, dailyScore)
            .flatMap(this::getNeighbours)
            .defaultIfEmpty(List.of())
            .onErrorResume(HttpStatusException.class, this::handleMissingPlayer);
    }

    private Mono<Integer> getPlayerPosition(UUID playerId, int dailyScore) {
        return leaderboardClient.getLeaderboardPosition(dailyScore)
            .flatMapMany(Flux::fromIterable)
            .filter(entry -> entry.playerId().equals(playerId))
            .next()
            .map(LeaderboardScoreResponse::position);
    }

    private Mono<List<LeaderboardResponse>> getNeighbours(int position) {
        int startPosition = Math.max(1, position - NEIGHBOURS_PER_SIDE);
        int endPosition = position + NEIGHBOURS_PER_SIDE;
        return leaderboardClient.getLeaderboard(startPosition, endPosition);
    }

    private Mono<List<LeaderboardResponse>> handleMissingPlayer(HttpStatusException error) {
        if (error.getStatus() == HttpStatus.NOT_FOUND) {
            return Mono.just(List.of());
        }
        return Mono.error(error);
    }

    private Mono<LeaderboardDto> addNamesToLeaderboard(
        List<LeaderboardResponse> neighbours,
        List<LeaderboardResponse> topTen
    ) {
        return getDisplayNames(neighbours, topTen)
            .map(names -> toLeaderboardDto(neighbours, topTen, names));
    }

    private Mono<Map<UUID, String>> getDisplayNames(
        List<LeaderboardResponse> neighbours,
        List<LeaderboardResponse> topTen
    ) {
        var playerIds = Stream.concat(neighbours.stream(), topTen.stream())
            .map(LeaderboardResponse::playerId)
            .distinct()
            .toList();

        return Flux.fromIterable(playerIds)
            .flatMap(this::getPlayerName)
            .collectMap(PlayerName::playerId, PlayerName::name);
    }

    private Mono<PlayerName> getPlayerName(UUID playerId) {
        return playerService.getPlayerDisplayName(playerId)
            .map(name -> toPlayerName(playerId, name));
    }

}
