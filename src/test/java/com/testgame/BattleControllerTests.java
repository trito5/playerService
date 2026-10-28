package com.testgame;

import com.testgame.api.dto.LevelDto;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.testgame.TestDataGenerator.createCompletedBattlesForToday;
import static com.testgame.TestDataGenerator.createPlayerId;
import static com.testgame.TestDataGenerator.createPreviousDayBattles;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.CONFLICT;
import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
public class BattleControllerTests extends AbstractPlayerApiTests {

    @Test
    @DisplayName("Starting a battle returns its ID and updates the player's daily start count and activity")
    public void testPost_battles_start_created() {

        var before = Instant.now();

        var response = apiClient.startBattleResponse(playerId, TestDataGenerator.LEVEL_IDS.get(0));
        var updatedPlayer = playerRepository.findById(playerId).blockOptional().orElseThrow();

        assertNotNull(response.getBody().orElseThrow().battleId());
        assertTrue(updatedPlayer.activity().isAfter(before));
        var state = apiClient.getPlayer(playerId);
        assertEquals(List.of(1, 0, 0), state.levels().stream().map(LevelDto::startCount).toList());
    }

    @Test
    @DisplayName("Each allowed attempt creates a distinct battle")
    public void testPost_battles_unique_ids_created() {

        var battleIds = IntStream.range(0, 3)
            .mapToObj(index -> apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0)).battleId())
            .toList();

        assertEquals(3, battleIds.stream().distinct().count());
        assertEquals(3, apiClient.getPlayer(playerId).levels().getFirst().startCount());
    }

    @Test
    @DisplayName("A player cannot start another battle after reaching the configured daily limit")
    public void testPost_battles_daily_limit_conflict() {
        for (int attempt = 0; attempt < 3; attempt++) {
            apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0));
        }
        var playerBeforeRejection = playerRepository.findById(playerId).blockOptional().orElseThrow();

        assertHttpStatus(() -> apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0)), CONFLICT);

        assertEquals(playerBeforeRejection, playerRepository.findById(playerId).blockOptional().orElseThrow());
        assertEquals(3, apiClient.getPlayer(playerId).levels().getFirst().startCount());
    }

    @Test
    @DisplayName("Concurrent starts for a new player cannot exceed the daily limit")
    public void testPost_battles_concurrent_new_player_limit_conflict() {
        var levelId = TestDataGenerator.LEVEL_IDS.getFirst();

        assertConcurrentStarts(Collections.nCopies(8, levelId), 3);

        assertEquals(3, apiClient.getPlayer(playerId).levels().getFirst().startCount());
        assertEquals(3L, battleRepository.count().block());
    }

    @Test
    @DisplayName("Only one concurrent start can consume the last remaining attempt")
    public void testPost_battles_concurrent_last_attempt_conflict() {
        var levelId = TestDataGenerator.LEVEL_IDS.getFirst();
        apiClient.startBattle(playerId, levelId);
        apiClient.startBattle(playerId, levelId);

        assertConcurrentStarts(Collections.nCopies(8, levelId), 1);

        assertEquals(3, apiClient.getPlayer(playerId).levels().getFirst().startCount());
        assertEquals(3L, battleRepository.count().block());
    }

    @Test
    @DisplayName("Concurrent starts enforce a separate daily limit for each level")
    public void testPost_battles_concurrent_level_limits_conflict() {
        var firstLevel = TestDataGenerator.LEVEL_IDS.get(0);
        var secondLevel = TestDataGenerator.LEVEL_IDS.get(1);

        assertConcurrentStarts(List.of(
            firstLevel, secondLevel, firstLevel, secondLevel,
            firstLevel, secondLevel, firstLevel, secondLevel), 6);

        assertEquals(List.of(3, 3, 0),
            apiClient.getPlayer(playerId).levels().stream().map(LevelDto::startCount).toList());
        assertEquals(6L, battleRepository.count().block());
    }

    @Test
    @DisplayName("The daily limit is independent for each player and level")
    public void testPost_battles_player_and_level_limits_created() {

        for (int attempt = 0; attempt < 3; attempt++) {
            apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0));
        }

        apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(1));
        apiClient.startBattle(otherPlayerId, TestDataGenerator.LEVEL_IDS.get(0));

        assertEquals(List.of(3, 1, 0),
            apiClient.getPlayer(playerId).levels().stream().map(LevelDto::startCount).toList());
        assertEquals(List.of(1, 0, 0),
            apiClient.getPlayer(otherPlayerId).levels().stream().map(LevelDto::startCount).toList());
    }

    @Test
    @DisplayName("Battles from the previous UTC day do not consume today's attempts")
    public void testPost_battles_previous_day_created() {
        saveBattles(createPreviousDayBattles(playerId, 3));

        apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0));

        assertEquals(1, apiClient.getPlayer(playerId).levels().getFirst().startCount());
    }

    @Test
    @DisplayName("Completed battles still consume an attempt")
    public void testPost_battles_completed_attempts_conflict() {
        saveBattles(createCompletedBattlesForToday(playerId, 3));
        assertHttpStatus(() -> apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.get(0)), CONFLICT);
        assertEquals(3, apiClient.getPlayer(playerId).levels().getFirst().startCount());
    }

    @Test
    @DisplayName("Starting a battle creates a missing player with zero daily score and current activity")
    public void testPost_battles_missing_player_created() {
        var before = Instant.now();

        var response = apiClient.startBattleResponse(playerId, TestDataGenerator.LEVEL_IDS.get(0));
        var player = playerRepository.findById(playerId).blockOptional().orElseThrow();
        var battle = battleRepository.findById(response.getBody().orElseThrow().battleId())
            .blockOptional().orElseThrow();

        assertNotNull(response.getBody().orElseThrow().battleId());
        assertEquals(0, battle.score());
        assertNull(battle.completedAt());
        assertTrue(player.activity().isAfter(before));
        var state = apiClient.getPlayer(playerId);
        assertEquals(1, state.levels().getFirst().startCount());
        assertTrue(state.leaderboardDto().playerPosition().isEmpty());
    }

    @Test
    @DisplayName("A level not present in config cannot be started")
    public void testPost_battles_missing_level_not_found() {
        apiClient.getPlayer(playerId);
        var unknownLevelId = createPlayerId();

        assertHttpStatus(() -> apiClient.startBattle(playerId, unknownLevelId), NOT_FOUND);

        assertEquals(List.of(0, 0, 0),
            apiClient.getPlayer(playerId).levels().stream().map(LevelDto::startCount).toList());
    }

    @Test
    @DisplayName("Malformed player or level IDs return bad request")
    public void testPost_battles_invalid_ids_bad_request() {
        assertHttpStatus(() -> apiClient.startBattleResponse(
            "not-a-uuid", TestDataGenerator.LEVEL_IDS.get(0).toString()), BAD_REQUEST);
        assertHttpStatus(() -> apiClient.startBattleResponse(playerId.toString(), "not-a-uuid"), BAD_REQUEST);
    }

    private void assertConcurrentStarts(List<UUID> levelIds, int expectedCreated) {
        var statuses = startConcurrently(levelIds);
        assertEquals((long) expectedCreated, statuses.stream().filter(CREATED::equals).count());
        assertEquals(levelIds.size() - (long) expectedCreated,
            statuses.stream().filter(CONFLICT::equals).count());
    }

    private List<HttpStatus> startConcurrently(List<UUID> levelIds) {
        var startSignal = new CompletableFuture<Void>();
        try (var executor = Executors.newFixedThreadPool(levelIds.size())) {
            var requests = new ArrayList<CompletableFuture<HttpStatus>>();
            for (var levelId : levelIds) {
                requests.add(CompletableFuture.supplyAsync(
                    () -> startAfterStartSignal(levelId, startSignal), executor));
            }

            startSignal.complete(null);
            return awaitStatuses(requests);
        }
    }

    private HttpStatus startAfterStartSignal(UUID levelId, CompletableFuture<Void> startSignal) {
        startSignal.join();
        try {
            return apiClient.startBattleResponse(playerId, levelId).getStatus();
        } catch (HttpClientResponseException error) {
            return error.getStatus();
        }
    }

    @SneakyThrows
    private List<HttpStatus> awaitStatuses(List<CompletableFuture<HttpStatus>> requests) {
        CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new))
            .get(30, TimeUnit.SECONDS);
        return requests.stream().map(CompletableFuture::join).toList();
    }

}
