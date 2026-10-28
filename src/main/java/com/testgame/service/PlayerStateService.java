package com.testgame.service;

import com.testgame.client.NewsClient;
import com.testgame.api.dto.LevelDto;
import com.testgame.api.dto.PlayerStateDto;
import com.testgame.mapper.NewsMapper;
import com.testgame.mapper.PlayerStateMapper;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import static com.testgame.mapper.PlayerStateMapper.toPlayerStateDto;
import static com.testgame.util.DateTimeUtil.todayUtc;

@Singleton
@RequiredArgsConstructor
public class PlayerStateService {

    private static final int NUMBER_OF_NEWS_TO_FETCH = 5;

    private final PlayerService playerService;
    private final BattleService battleService;
    private final ConfigService configService;
    private final LeaderboardService leaderboardService;
    private final NewsClient newsClient;

    public Mono<PlayerStateDto> getPlayer(UUID playerId) {
        return playerService.recordPlayerActivity(playerId)
            .flatMap(player -> getPlayerState(playerId, player.activity()));
    }

    private Mono<PlayerStateDto> getPlayerState(UUID playerId, Instant activity) {
        var day = todayUtc();
        var levels = getLevels(playerId, day);
        var leaderboard = battleService.getScoreForDay(playerId, day)
            .flatMap(score -> leaderboardService.getLeaderboard(playerId, score));
        var news = newsClient.getNews(NUMBER_OF_NEWS_TO_FETCH).map(NewsMapper::toNewsDtos);

        return Mono.zip(levels, leaderboard, news)
            .map(result -> toPlayerStateDto(result.getT1(), result.getT2(), result.getT3(), activity));
    }

    private Mono<List<LevelDto>> getLevels(UUID playerId, LocalDate day) {
        return battleService.countStartsByLevel(playerId, day)
            .collectList()
            .zipWith(configService.getConfig(), PlayerStateMapper::toLevelDtos);
    }
}
