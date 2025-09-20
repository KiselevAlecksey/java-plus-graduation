package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
@Slf4j
public class StatWebClient {
    private final DiscoveryClient discoveryClient;

    @Value("${service.name}")
    private String serviceName;

    private volatile String urlGateway = "http://localhost:8080";
    private final Object lock = new Object();

    public StatWebClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Retryable(
            retryFor = {StatsServerUnavailable.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    private ServiceInstance discoverServiceInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances == null || instances.isEmpty()) {
                throw new StatsServerUnavailable("Stats service not found in discovery: " + serviceName);
            }
            return instances.getFirst();
        } catch (Exception exception) {
            log.error("Failed to discover stats service: {}", serviceName, exception);
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики: " + serviceName,
                    exception
            );
        }
    }

    private WebClient createWebClient() {
        return WebClient.create(urlGateway);
    }

    public HitDto addHit(HitDto hitDto) {
        return createWebClient()
                .post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(hitDto))
                .retrieve()
                .bodyToMono(HitDto.class)
                .block();
    }

    public Mono<StatDto> get(StatRequestDto request) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(urlGateway + "/stats");

        uriBuilder.queryParam("start", request.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        uriBuilder.queryParam("end", request.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (request.getUri() != null) {
            for (String uri : request.getUri()) {
                uriBuilder.queryParam("uris", uri);
            }
        }
        uriBuilder.queryParam("unique", request.getUnique());

        return createWebClient()
                .get()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .bodyToMono(StatDto.class);
    }

    public Long getEventViews(String requestUri) {
        return createWebClient()
                .get()
                .uri("/stats/event?uri={uri}", requestUri)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }
}
