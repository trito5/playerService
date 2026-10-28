package com.testgame.client;

import com.testgame.domain.LeaderboardEntry;
import com.testgame.client.dto.LeaderboardResponse;
import com.testgame.client.dto.LeaderboardScoreResponse;
import com.testgame.client.dto.SaveScoreRequest;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

import static com.testgame.mapper.LeaderboardMapper.toLeaderboardEntry;
import static com.testgame.mapper.LeaderboardMapper.toLeaderboardResponse;
import static com.testgame.mapper.LeaderboardMapper.toLeaderboardScoreResponse;

@Singleton
@Requires(property = "leaderboard.mock.enabled", value = "true")
public class MockLeaderboardClient implements LeaderboardClient {

    private static final List<LeaderboardEntry> INITIAL_ENTRIES = List.of(
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000001"), 100),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000002"), 90),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000003"), 80),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000004"), 70),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000005"), 60),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000006"), 50),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000007"), 40),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000008"), 30),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000009"), 20),
        toLeaderboardEntry(UUID.fromString("00000000-0000-0000-0000-000000000010"), 10)
    );

    private final ConcurrentHashMap<UUID, LeaderboardEntry> entries = createInitialEntries();

    @Override
    public Mono<LeaderboardScoreResponse> updatePlayerScore(SaveScoreRequest request) {
        return Mono.fromSupplier(() -> {
            saveScore(request);
            return getPlayerPosition(request.playerId());
        });
    }

    @Override
    public Mono<List<LeaderboardScoreResponse>> getLeaderboardPosition(int score) {
        return Mono.fromSupplier(() -> getPositionsForScore(score));
    }

    @Override
    public Mono<List<LeaderboardResponse>> getLeaderboard(int startPosition, int endPosition) {
        if (startPosition > endPosition) {
            return Mono.error(new HttpStatusException(
                HttpStatus.BAD_REQUEST, "startPosition must not exceed endPosition"));
        }
        return Mono.fromSupplier(() -> getEntriesBetween(startPosition, endPosition));
    }

    public void reset() {
        entries.clear();
        entries.putAll(createInitialEntries());
    }

    private void saveScore(SaveScoreRequest request) {
        entries.compute(request.playerId(), (id, current) -> keepHighestScore(current, request));
    }

    private LeaderboardEntry keepHighestScore(LeaderboardEntry current, SaveScoreRequest request) {
        if (current != null && current.score() >= request.score()) {
            return current;
        }
        return toLeaderboardEntry(request.playerId(), request.score());
    }

    private List<LeaderboardScoreResponse> getPositionsForScore(int score) {
        return getRankedEntries().stream()
            .filter(entry -> entry.score() == score)
            .map(entry -> toLeaderboardScoreResponse(entry.playerId(), entry.position()))
            .toList();
    }

    private List<LeaderboardResponse> getEntriesBetween(int startPosition, int endPosition) {
        return getRankedEntries().stream()
            .filter(entry -> entry.position() >= startPosition && entry.position() <= endPosition)
            .toList();
    }

    private LeaderboardScoreResponse getPlayerPosition(UUID playerId) {
        var player = getRankedEntries().stream()
            .filter(entry -> entry.playerId().equals(playerId))
            .findFirst()
            .orElseThrow();
        return toLeaderboardScoreResponse(player.playerId(), player.position());
    }

    private ConcurrentHashMap<UUID, LeaderboardEntry> createInitialEntries() {
        var entries = new ConcurrentHashMap<UUID, LeaderboardEntry>();
        for (var entry : INITIAL_ENTRIES) {
            entries.put(entry.playerId(), entry);
        }
        return entries;
    }

    private List<LeaderboardEntry> getSortedEntries() {
        return entries.values().stream()
            .sorted(Comparator.comparingInt(LeaderboardEntry::score).reversed()
                .thenComparing(LeaderboardEntry::playerId))
            .toList();
    }

    private List<LeaderboardResponse> getRankedEntries() {
        var sortedEntries = getSortedEntries();
        var rankedEntries = new ArrayList<LeaderboardResponse>(sortedEntries.size());
        for (int index = 0; index < sortedEntries.size(); index++) {
            var entry = sortedEntries.get(index);
            rankedEntries.add(toLeaderboardResponse(entry.playerId(), entry.score(), index + 1));
        }
        return List.copyOf(rankedEntries);
    }
}
