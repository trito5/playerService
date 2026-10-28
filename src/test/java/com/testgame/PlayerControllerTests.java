package com.testgame;

import com.testgame.api.dto.LevelDto;
import com.testgame.api.dto.PositionDto;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
public class PlayerControllerTests extends AbstractPlayerApiTests {

    @Test
    @DisplayName("A new player receives levels, leaderboard, news and activity")
    public void testGet_players_new_player_ok() {
        var before = Instant.now();

        var state = apiClient.getPlayer(playerId);

        assertEquals(TestDataGenerator.LEVEL_IDS, state.levels().stream().map(LevelDto::levelId).toList());
        assertEquals(List.of("Level 1", "Level 2", "Level 3"),
            state.levels().stream().map(LevelDto::levelName).toList());
        assertTrue(state.levels().stream().allMatch(level -> level.startCount() == 0));
        assertTrue(state.leaderboardDto().playerPosition().isEmpty());
        assertEquals(10, state.leaderboardDto().topTenPositions().size());
        assertDisplayNames(state.leaderboardDto().playerPosition());
        assertDisplayNames(state.leaderboardDto().topTenPositions());
        assertEquals(5, state.news().size());
        assertFalse(state.activity().isBefore(before));
        assertFalse(state.activity().isAfter(Instant.now()));
        assertEquals(playerRepository.findById(playerId).blockOptional().orElseThrow().activity(),
            state.activity());
    }

    @Test
    @DisplayName("An existing player receives the updated activity stored in the database")
    public void testGet_players_existing_player_activity_ok() {
        var player = TestDataGenerator.createPlayer(playerId);
        savePlayer(player);

        var state = apiClient.getPlayer(playerId);
        var updatedPlayer = playerRepository.findById(playerId).blockOptional().orElseThrow();

        assertTrue(updatedPlayer.activity().isAfter(player.activity()));
        assertEquals(updatedPlayer.activity(), state.activity());
    }

    @Test
    @DisplayName("Only the player's starts within today's UTC date are counted")
    public void testGet_players_daily_starts_ok() {
        saveBattles(TestDataGenerator.createBattleHistory(playerId, otherPlayerId));

        var state = apiClient.getPlayer(playerId);

        assertEquals(TestDataGenerator.LEVEL_IDS, state.levels().stream().map(LevelDto::levelId).toList());
        assertEquals(List.of(3, 1, 0), state.levels().stream().map(LevelDto::startCount).toList());
    }

    @Test
    @DisplayName("First place returns only the available neighbours")
    public void testGet_players_first_place_ok() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();
        apiClient.completeBattle(playerId, battleId, 150);

        var state = apiClient.getPlayer(playerId);

        assertEquals(List.of(1, 2, 3),
            state.leaderboardDto().playerPosition().stream().map(PositionDto::position).toList());
        assertEquals(List.of(150, 100, 90),
            state.leaderboardDto().playerPosition().stream().map(PositionDto::score).toList());
    }

    @Test
    @DisplayName("A range extending past the leaderboard returns its available entries")
    public void testGet_players_near_end_ok() {
        var battleId = apiClient.startBattle(playerId, TestDataGenerator.LEVEL_IDS.getFirst()).battleId();
        apiClient.completeBattle(playerId, battleId, 15);

        var state = apiClient.getPlayer(playerId);

        assertEquals(List.of(8, 9, 10, 11),
            state.leaderboardDto().playerPosition().stream().map(PositionDto::position).toList());
    }

    @Test
    @DisplayName("Malformed player IDs return bad request")
    public void testGet_players_invalid_id_bad_request() {
        assertHttpStatus(() -> apiClient.getPlayer("not-a-uuid"), BAD_REQUEST);
    }

    @Test
    @DisplayName("Daily score overflow returns an internal server error")
    public void testGet_players_daily_score_overflow_internal_server_error() {
        saveBattles(TestDataGenerator.createOverflowingDailyScoreBattles(playerId));

        assertHttpStatus(() -> apiClient.getPlayer(playerId), INTERNAL_SERVER_ERROR);
    }
}
