package com.testgame;

import com.testgame.database.BattleEntity;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.testgame.TestDataGenerator.createBattle;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.CONFLICT;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
public class CompleteBattleControllerTests extends AbstractPlayerApiTests {

    @Test
    @DisplayName("Completing a started battle persists its score, timestamps and activity and publishes its rank")
    public void testPost_battles_complete_ok() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();
        var started = persistedBattle(battleId);
        var before = Instant.now();

        var response = apiClient.completeBattleResponse(playerId, battleId, 55);
        var completed = persistedBattle(battleId);
        var player = playerRepository.findById(playerId).blockOptional().orElseThrow();

        assertEquals(6, response.getBody().orElseThrow().position());
        assertEquals(55, completed.score());
        assertEquals(started.battleId(), completed.battleId());
        assertEquals(started.playerId(), completed.playerId());
        assertEquals(started.levelId(), completed.levelId());
        assertEquals(started.startedAt(), completed.startedAt());
        assertNotNull(completed.completedAt());
        assertEquals(completed.completedAt(), completed.updatedAt());
        assertTrue(player.activity().isAfter(before));
        assertPublishedScore(playerId, 55);
        assertTrue(apiClient.getPlayer(playerId).leaderboardDto().topTenPositions().stream()
            .anyMatch(entry -> entry.playerId().equals(playerId) && entry.score() == 55 && entry.position() == 6));
    }

    @Test
    @DisplayName("Multiple completions publish the absolute daily total across levels without duplicating the player")
    public void testPost_battles_complete_daily_total_ok() {
        var first = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0)).battleId();
        var second = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(1)).battleId();
        var third = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0)).battleId();

        assertEquals(9, apiClient.completeBattle(playerId, first, 25).position());
        assertEquals(6, apiClient.completeBattle(playerId, second, 30).position());
        assertEquals(1, apiClient.completeBattle(playerId, third, 65).position());

        var state = apiClient.getPlayer(playerId);
        assertPublishedScore(playerId, 120);
        assertEquals(playerId, state.leaderboardDto().topTenPositions().getFirst().playerId());
        assertEquals(1L, state.leaderboardDto().topTenPositions().stream()
            .filter(entry -> entry.playerId().equals(playerId)).count());
    }

    @Test
    @DisplayName("Daily totals include completed battles across levels but exclude unfinished battles and other players")
    public void testPost_battles_complete_daily_score_history_ok() {
        saveBattles(TestDataGenerator.createDailyScoreHistory(playerId, otherPlayerId));
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(2)).battleId();

        apiClient.completeBattle(playerId, battleId, 1);

        assertPublishedScore(playerId, 56);
    }

    @Test
    @DisplayName("Previous-day points and another player's points are excluded from the published daily total")
    public void testPost_battles_complete_isolated_daily_total_ok() {
        var midnight = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant();
        saveBattles(List.of(
            createBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0),
                midnight.minusSeconds(120), midnight.minusSeconds(60), 500),
            createBattle(otherPlayerId, TestDataGenerator.LEVEL_IDS.get(0),
                midnight, midnight, 700)));
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(1)).battleId();

        assertEquals(6, apiClient.completeBattle(playerId, battleId, 55).position());

        assertPublishedScore(playerId, 55);
        assertEquals(500, battleRepository
            .findByPlayerIdAndCompletedAtGreaterThanEqualsAndCompletedAtLessThan(
                playerId, midnight.minus(Duration.ofDays(1)), midnight)
            .map(BattleEntity::score).reduce(0, Integer::sum).block());
    }

    @Test
    @DisplayName("A battle started yesterday contributes to the UTC day when it is completed")
    public void testPost_battles_complete_previous_day_start_ok() {
        var battle = createBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst(),
            Instant.now().minus(Duration.ofDays(1)), null, 0);
        saveBattles(List.of(battle));
        var started = persistedBattle(battle.battleId());

        assertEquals(6, apiClient.completeBattle(playerId, battle.battleId(), 55).position());

        assertEquals(started.startedAt(), persistedBattle(battle.battleId()).startedAt());
        assertPublishedScore(playerId, 55);
        assertNotNull(playerRepository.findById(playerId).block());
    }

    @Test
    @DisplayName("Completing a battle without a previous start returns not found")
    public void testPost_battles_complete_missing_battle_not_found() {
        assertHttpStatus(() -> apiClient.completeBattle(playerId, TestDataGenerator.createPlayerId(), 55),
            NOT_FOUND);
    }

    @Test
    @DisplayName("A different player cannot complete the owner's battle")
    public void testPost_battles_complete_wrong_owner_not_found() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();

        assertHttpStatus(() -> apiClient.completeBattle(otherPlayerId, battleId, 55), NOT_FOUND);

        var battle = persistedBattle(battleId);
        assertEquals(0, battle.score());
        assertNull(battle.completedAt());
    }

    @Test
    @DisplayName("A completion rejects different scores without changing points")
    public void testPost_battles_complete_duplicate_conflict() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();
        apiClient.completeBattle(playerId, battleId, 55);
        var completed = persistedBattle(battleId);

        assertHttpStatus(() -> apiClient.completeBattle(playerId, battleId, 55), CONFLICT);
        assertHttpStatus(() -> apiClient.completeBattle(playerId, battleId, 100), CONFLICT);

        assertEquals(completed, persistedBattle(battleId));
        assertPublishedScore(playerId, 55);
    }

    @Test
    @SneakyThrows
    @DisplayName("Only one concurrent completion succeeds when scores differ")
    public void testPost_battles_complete_concurrent_different_scores_conflict() {
        assertSingleConcurrentCompletion(List.of(51, 52, 53, 54, 55, 56, 57, 58));
    }

    @Test
    @SneakyThrows
    @DisplayName("Only one concurrent completion succeeds when scores are identical")
    public void testPost_battles_complete_concurrent_same_score_conflict() {
        assertSingleConcurrentCompletion(List.of(55, 55, 55, 55, 55, 55, 55, 55));
    }

    @Test
    @DisplayName("Zero and negative scores are rejected without completing the battle")
    public void testPost_battles_complete_non_positive_score_bad_request() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();

        assertHttpStatus(() -> apiClient.completeBattle(playerId, battleId, 0), BAD_REQUEST);
        assertHttpStatus(() -> apiClient.completeBattle(playerId, battleId, -1), BAD_REQUEST);

        assertEquals(0, persistedBattle(battleId).score());
        assertNull(persistedBattle(battleId).completedAt());
    }

    @Test
    @DisplayName("Malformed player IDs, battle IDs and scores return bad request")
    public void testPost_battles_complete_malformed_path_bad_request() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();
        assertHttpStatus(() -> apiClient.completeBattleResponse(
            "not-a-uuid", battleId.toString(), "55"), BAD_REQUEST);
        assertHttpStatus(() -> apiClient.completeBattleResponse(
            playerId.toString(), "not-a-uuid", "55"), BAD_REQUEST);
        assertHttpStatus(() -> apiClient.completeBattleResponse(
            playerId.toString(), battleId.toString(), "not-a-number"), BAD_REQUEST);
        assertHttpStatus(() -> apiClient.completeBattleResponse(
            playerId.toString(), battleId.toString(), "2147483648"), BAD_REQUEST);
        assertNull(persistedBattle(battleId).completedAt());
    }

    @SneakyThrows
    private void assertSingleConcurrentCompletion(List<Integer> scores) {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();
        var statuses = completeConcurrently(battleId, scores);

        assertEquals(1L, statuses.stream().filter(OK::equals).count());
        assertEquals(scores.size() - 1L, statuses.stream().filter(CONFLICT::equals).count());
        var winningScore = scores.get(statuses.indexOf(OK));
        var completed = persistedBattle(battleId);
        assertEquals(winningScore.intValue(), completed.score());
        assertNotNull(completed.completedAt());
        assertEquals(completed.completedAt(), completed.updatedAt());

        assertHttpStatus(() -> apiClient.completeBattle(playerId, battleId, 100), CONFLICT);

        assertEquals(completed, persistedBattle(battleId));
        assertPublishedScore(playerId, winningScore);
    }

    private List<HttpStatus> completeConcurrently(UUID battleId, List<Integer> scores) {
        var startSignal = new CompletableFuture<Void>();
        try (var executor = Executors.newFixedThreadPool(scores.size())) {
            var requests = new ArrayList<CompletableFuture<HttpStatus>>();
            for (var score : scores) {
                requests.add(CompletableFuture.supplyAsync(
                    () -> completeAfterStartSignal(battleId, score, startSignal), executor));
            }

            startSignal.complete(null);
            return awaitStatuses(requests);
        }
    }

    private HttpStatus completeAfterStartSignal(UUID battleId, int score, CompletableFuture<Void> startSignal) {
        startSignal.join();
        return completeBattleStatus(battleId, score);
    }

    @SneakyThrows
    private List<HttpStatus> awaitStatuses(List<CompletableFuture<HttpStatus>> requests) {
        CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new))
            .get(30, TimeUnit.SECONDS);
        return requests.stream().map(CompletableFuture::join).toList();
    }

    private HttpStatus completeBattleStatus(UUID battleId, int score) {
        try {
            return apiClient.completeBattleResponse(playerId, battleId, score).getStatus();
        } catch (HttpClientResponseException error) {
            return error.getStatus();
        }
    }

    private BattleEntity persistedBattle(UUID battleId) {
        return battleRepository.findById(battleId).blockOptional().orElseThrow();
    }

    private void assertPublishedScore(UUID id, int expectedScore) {
        var entries = apiClient.getPlayer(id).leaderboardDto().playerPosition().stream()
            .filter(entry -> entry.playerId().equals(id)).toList();
        assertEquals(1, entries.size());
        assertEquals(expectedScore, entries.getFirst().score());
        assertDisplayNames(entries);
    }
}
