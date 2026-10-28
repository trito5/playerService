package com.testgame.mapper;

import com.testgame.domain.LevelStartCount;
import com.testgame.api.dto.LeaderboardDto;
import com.testgame.api.dto.LevelDto;
import com.testgame.api.dto.NewsDto;
import com.testgame.api.dto.PlayerStateDto;
import com.testgame.client.dto.ConfigResponse;
import com.testgame.client.dto.LevelConfigResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerStateMapper {

    public static PlayerStateDto toPlayerStateDto(
        List<LevelDto> levels,
        LeaderboardDto leaderboard,
        List<NewsDto> news,
        Instant activity
    ) {
        return PlayerStateDto.builder()
            .levels(levels)
            .leaderboardDto(leaderboard)
            .news(news)
            .activity(activity)
            .build();
    }

    public static List<LevelDto> toLevelDtos(List<LevelStartCount> counts, ConfigResponse config) {
        Map<UUID, Long> starts = counts.stream()
            .collect(Collectors.toMap(LevelStartCount::levelId, LevelStartCount::startCount));
        return config.levels().stream()
            .map(level -> toLevelDto(level, starts.getOrDefault(level.levelId(), 0L)))
            .toList();
    }

    public static LevelDto toLevelDto(LevelConfigResponse level, long startCount) {
        return LevelDto.builder()
            .levelId(level.levelId())
            .levelName(level.levelName())
            .startCount(Math.toIntExact(startCount))
            .build();
    }

}
