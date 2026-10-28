package com.testgame.mapper;

import com.testgame.api.dto.NewsDto;
import com.testgame.client.dto.NewsResponse;
import java.time.Instant;
import java.util.List;
import lombok.NonNull;

public class NewsMapper {

    public static List<NewsDto> toNewsDtos(List<NewsResponse> news) {
        return news.stream()
            .map(NewsMapper::toNewsDto)
            .toList();
    }

    public static NewsDto toNewsDto(NewsResponse news) {
        return NewsDto.builder()
            .message(news.message())
            .type(news.type())
            .timestamp(news.timestamp())
            .build();
    }

    public static NewsResponse toNewsResponse(
        @NonNull String message,
        @NonNull String type,
        @NonNull Instant timestamp
    ) {
        return NewsResponse.builder()
            .message(message)
            .type(type)
            .timestamp(timestamp)
            .build();
    }
}
