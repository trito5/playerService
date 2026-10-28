package com.testgame;

import com.testgame.database.BattleEntity;
import com.testgame.database.PlayerEntity;
import io.micronaut.http.client.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class TestDataGenerator {

    public static final List<UUID> LEVEL_IDS = List.of(
        UUID.fromString("10000000-0000-0000-0000-000000000001"),
        UUID.fromString("10000000-0000-0000-0000-000000000002"),
        UUID.fromString("10000000-0000-0000-0000-000000000003"));

    public static UUID createPlayerId() {
        return UUID.randomUUID();
    }

    public static TestApiClient createApiClient(HttpClient client) {
        return TestApiClient.builder().client(client).build();
    }

    public static PlayerEntity createPlayer(UUID playerId) {
        return PlayerEntity.builder()
            .playerId(playerId)
            .activity(Instant.now().minus(Duration.ofDays(1)))
            .build();
    }

    public static BattleEntity createBattle(UUID playerId, UUID levelId, Instant startedAt) {
        return createBattle(playerId, levelId, startedAt, null);
    }

    private static BattleEntity createBattle(
        UUID playerId, UUID levelId, Instant startedAt, Instant completedAt
    ) {
        return createBattle(playerId, levelId, startedAt, completedAt, 10);
    }

    public static BattleEntity createBattle(
        UUID playerId, UUID levelId, Instant startedAt, Instant completedAt, int score
    ) {
        return BattleEntity.builder()
            .battleId(UUID.randomUUID())
            .playerId(playerId)
            .levelId(levelId)
            .score(score)
            .startedAt(startedAt)
            .updatedAt(startedAt)
            .completedAt(completedAt)
            .build();
    }

    public static List<BattleEntity> createDailyScoreHistory(UUID playerId, UUID otherPlayerId) {
        var today = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate()
            .atStartOfDay(ZoneId.of("UTC")).toInstant();
        return List.of(
            createBattle(playerId, LEVEL_IDS.getFirst(), today.minusSeconds(3600), today.plusSeconds(60), 17),
            createBattle(playerId, LEVEL_IDS.get(1), today.minus(Duration.ofDays(30)),
                today.plusSeconds(120), 23),
            createBattle(playerId, UUID.randomUUID(), today, today.plusSeconds(180), 15),
            createBattle(playerId, LEVEL_IDS.getFirst(), today, null, 100),
            createBattle(playerId, LEVEL_IDS.get(1), today.minusSeconds(120),
                today.minusSeconds(60), 200),
            createBattle(otherPlayerId, LEVEL_IDS.getFirst(), today, today.plusSeconds(60), 300));
    }

    public static List<BattleEntity> createOverflowingDailyScoreBattles(UUID playerId) {
        var now = Instant.now();
        return List.of(
            createBattle(playerId, LEVEL_IDS.getFirst(), now, now, Integer.MAX_VALUE),
            createBattle(playerId, LEVEL_IDS.get(1), now, now, 1));
    }

    public static List<BattleEntity> createPreviousDayBattles(UUID playerId, int numberOfBattles) {
        var yesterday = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate()
            .minusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
        return IntStream.range(0, numberOfBattles)
            .mapToObj(index -> createBattle(playerId, LEVEL_IDS.getFirst(), yesterday.plusSeconds(index)))
            .toList();
    }

    public static List<BattleEntity> createCompletedBattlesForToday(UUID playerId, int numberOfBattles) {
        var now = Instant.now();
        return IntStream.range(0, numberOfBattles)
            .mapToObj(index -> createBattle(playerId, LEVEL_IDS.getFirst(), now, now))
            .toList();
    }

    public static List<BattleEntity> createBattleHistory(UUID playerId, UUID otherPlayerId) {
        var day = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate();
        var today = day.atStartOfDay(ZoneId.of("UTC")).toInstant();
        var tomorrow = day.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
        return List.of(
            createBattle(playerId, LEVEL_IDS.getFirst(), today.minusNanos(1000)),
            createBattle(playerId, LEVEL_IDS.getFirst(), today),
            createBattle(playerId, LEVEL_IDS.getFirst(), today.plusSeconds(3600)),
            createBattle(playerId, LEVEL_IDS.getFirst(), tomorrow.minusNanos(1000)),
            createBattle(playerId, LEVEL_IDS.get(1), today.plusSeconds(7200)),
            createBattle(playerId, LEVEL_IDS.getFirst(), tomorrow),
            createBattle(playerId, LEVEL_IDS.getFirst(), day.minusMonths(1)
                .atStartOfDay(ZoneId.of("UTC")).toInstant()),
            createBattle(playerId, LEVEL_IDS.getFirst(), day.minusYears(1)
                .atStartOfDay(ZoneId.of("UTC")).toInstant()),
            createBattle(otherPlayerId, LEVEL_IDS.getFirst(), today));
    }

    public static String createDisplayName(UUID playerId) {
        return "Player-" + playerId;
    }

}
