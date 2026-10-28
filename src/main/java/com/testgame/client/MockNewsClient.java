package com.testgame.client;

import com.testgame.client.dto.NewsResponse;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import reactor.core.publisher.Mono;

import static com.testgame.mapper.NewsMapper.toNewsResponse;

@Singleton
@Requires(property = "news.mock.enabled", value = "true")
public class MockNewsClient implements NewsClient {

    private static final List<NewsResponse> NEWS = List.of(
        toNewsResponse("A new season has started.", "SEASON", Instant.parse("2026-10-26T12:00:00Z")),
        toNewsResponse("A new level is available.", "LEVEL", Instant.parse("2026-10-25T12:00:00Z")),
        toNewsResponse("The weekend tournament is open.", "TOURNAMENT", Instant.parse("2026-10-24T12:00:00Z")),
        toNewsResponse("Leaderboard rewards have been updated.", "REWARD", Instant.parse("2026-10-23T12:00:00Z")),
        toNewsResponse("A new game update is available.", "UPDATE", Instant.parse("2026-10-22T12:00:00Z")),
        toNewsResponse("Scheduled maintenance is complete.", "MAINTENANCE", Instant.parse("2026-10-21T12:00:00Z")),
        toNewsResponse("Welcome to the community event.", "EVENT", Instant.parse("2026-10-20T12:00:00Z"))
    );

    @Override
    public Mono<List<NewsResponse>> getNews(int count) {
        if (count < 0) {
            return Mono.error(new HttpStatusException(
                HttpStatus.BAD_REQUEST, "count must not be negative"));
        }
        return Mono.just(NEWS.stream().limit(count).toList());
    }
}
