package com.testgame.client;

import com.testgame.client.dto.NewsResponse;
import java.util.List;
import reactor.core.publisher.Mono;

public interface NewsClient {

    Mono<List<NewsResponse>> getNews(int count);
}
