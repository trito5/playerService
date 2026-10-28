package com.testgame.client;

import com.testgame.client.dto.LeaderboardResponse;
import com.testgame.client.dto.LeaderboardScoreResponse;
import com.testgame.client.dto.SaveScoreRequest;
import java.util.List;
import reactor.core.publisher.Mono;

public interface LeaderboardClient {

    Mono<List<LeaderboardScoreResponse>> getLeaderboardPosition(int score);

    Mono<LeaderboardScoreResponse> updatePlayerScore(SaveScoreRequest request);

    Mono<List<LeaderboardResponse>> getLeaderboard(int startPosition, int endPosition);
}
