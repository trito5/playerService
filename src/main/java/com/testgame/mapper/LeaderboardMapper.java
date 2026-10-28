package com.testgame.mapper;

import com.testgame.domain.LeaderboardEntry;
import com.testgame.domain.PlayerDailyScore;
import com.testgame.api.dto.LeaderboardDto;
import com.testgame.api.dto.PositionDto;
import com.testgame.client.dto.LeaderboardResponse;
import com.testgame.client.dto.LeaderboardScoreResponse;
import com.testgame.client.dto.SaveScoreRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;

public class LeaderboardMapper {

    public static SaveScoreRequest toSaveScoreRequest(PlayerDailyScore score) {
        return SaveScoreRequest.builder()
            .playerId(score.playerId())
            .score(score.score())
            .build();
    }

    public static LeaderboardEntry toLeaderboardEntry(UUID playerId, int score) {
        return LeaderboardEntry.builder()
            .playerId(playerId)
            .score(score)
            .build();
    }

    public static LeaderboardDto toLeaderboardDto(
        List<LeaderboardResponse> neighbours, List<LeaderboardResponse> topTen, Map<UUID, String> names
    ) {
        return LeaderboardDto.builder()
            .playerPosition(toPositionDtos(neighbours, names))
            .topTenPositions(toPositionDtos(topTen, names))
            .build();
    }

    private static List<PositionDto> toPositionDtos(
        List<LeaderboardResponse> entries, Map<UUID, String> names
    ) {
        return entries.stream()
            .map(entry -> toPositionDto(
                entry.playerId(), names.get(entry.playerId()), entry.position(), entry.score()))
            .toList();
    }

    public static PositionDto toPositionDto(UUID playerId, String playerName, int position, int score) {
        return PositionDto.builder()
            .playerId(playerId)
            .playerName(playerName)
            .position(position)
            .score(score)
            .build();
    }

    public static LeaderboardScoreResponse toLeaderboardScoreResponse(UUID playerId, int position) {
        return LeaderboardScoreResponse.builder()
            .playerId(playerId)
            .position(position)
            .build();
    }

    public static LeaderboardResponse toLeaderboardResponse(@NonNull UUID playerId, int score, int position) {
        return LeaderboardResponse.builder()
            .playerId(playerId)
            .score(score)
            .position(position)
            .build();
    }
}
