package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;


@Component
public class StatWebClient {
    private final WebClient webClient;

    public String url;

    public StatWebClient(@Value("${stats.url:http://localhost:9090}") String url) {
        this.url = url;
        webClient = WebClient.create(url);
    }

    public HitDto addHit(HitDto hitDto) {
        return webClient
                .post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(hitDto))
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        return Mono.error(new RuntimeException("Server Error"));
                    } else if (clientResponse.statusCode().is4xxClientError()) {
                        return Mono.error(new RuntimeException("Client Error"));
                    } else {
                        return clientResponse.bodyToMono(HitDto.class);
                    }
                })
                .block();
    }

    public Mono<StatDto> get(StatRequestDto request) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:9090/stats");

        uriBuilder.queryParam("start", request.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        uriBuilder.queryParam("end", request.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (request.getUri() != null) {
            for (String uri : request.getUri()) {
                uriBuilder.queryParam("uris", uri);
            }
        }
        uriBuilder.queryParam("unique", request.getUnique());
        return webClient
                .get()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .bodyToMono(StatDto.class);

    }

    public Long getEventViews(String request) {
      UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://stats-server:9090/stats/event");
                uriBuilder.queryParam("uri", request);
        return webClient
                .get()
                .uri(uriBuilder.build().toUri())
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        return Mono.error(new RuntimeException("Server Error"));
                    } else if (clientResponse.statusCode().is4xxClientError()) {
                        return Mono.error(new RuntimeException("Client Error"));
                    } else {
                        return clientResponse.bodyToMono(Long.class);
                    }
                })
                .block();
    }

}
