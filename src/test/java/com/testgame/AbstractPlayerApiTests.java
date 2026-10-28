package com.testgame;

import com.testgame.client.MockLeaderboardClient;
import com.testgame.database.BattleEntity;
import com.testgame.database.BattleRepository;
import com.testgame.database.PlayerEntity;
import com.testgame.database.PlayerRepository;
import com.testgame.api.dto.PositionDto;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractPlayerApiTests implements TestPropertyProvider {

    private static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:17.6");

    static {
        DATABASE.start();
    }

    @Override
    public Map<String, String> getProperties() {
        return Map.of(
            "datasources.default.url", DATABASE.getJdbcUrl(),
            "datasources.default.username", DATABASE.getUsername(),
            "datasources.default.password", DATABASE.getPassword(),
            "r2dbc.datasources.default.url", "r2dbc:pool:postgresql://%s:%d/%s".formatted(
                DATABASE.getHost(), DATABASE.getMappedPort(5432), DATABASE.getDatabaseName()),
            "r2dbc.datasources.default.username", DATABASE.getUsername(),
            "r2dbc.datasources.default.password", DATABASE.getPassword()
        );
    }

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    PlayerRepository playerRepository;

    @Inject
    BattleRepository battleRepository;

    @Inject
    MockLeaderboardClient mockLeaderboardClient;

    protected TestApiClient apiClient;
    protected UUID playerId;
    protected UUID otherPlayerId;

    @BeforeEach
    void setUp() {
        apiClient = TestDataGenerator.createApiClient(client);
        playerId = TestDataGenerator.createPlayerId();
        otherPlayerId = TestDataGenerator.createPlayerId();
    }

    @AfterEach
    void cleanUp() {
        battleRepository.deleteByPlayerIdIn(List.of(playerId, otherPlayerId))
            .then(playerRepository.deleteById(playerId))
            .then(playerRepository.deleteById(otherPlayerId))
            .block();
        mockLeaderboardClient.reset();
    }

    protected void savePlayer(PlayerEntity player) {
        playerRepository.save(player).block();
    }

    protected void saveBattles(List<BattleEntity> battles) {
        battleRepository.saveAll(battles).collectList().block();
    }

    protected void assertHttpStatus(Runnable request, HttpStatus expectedStatus) {
        var error = assertThrows(HttpClientResponseException.class, request::run);
        assertEquals(expectedStatus, error.getStatus());
    }

    protected void assertDisplayNames(List<PositionDto> positions) {
        for (var position : positions) {
            assertEquals(TestDataGenerator.createDisplayName(position.playerId()), position.playerName());
        }
    }

}
