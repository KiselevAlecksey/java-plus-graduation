package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
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

    private volatile String cachedUrl;
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

    private String getStatsServiceUrl() {
        if (cachedUrl == null) {
            synchronized (lock) {
                if (cachedUrl == null) {
                    ServiceInstance instance = discoverServiceInstance(); // ← Вызов с ретраями
                    cachedUrl = instance.getUri().toString();
                    log.info("Discovered stats service at: {}", cachedUrl);
                }
            }
        }
        return cachedUrl;
    }

    private WebClient createWebClient() {
        String baseUrl = getStatsServiceUrl();
        return WebClient.create(baseUrl);
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
        String baseUrl = getStatsServiceUrl();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "/stats");

        uriBuilder.queryParam("start", request.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        uriBuilder.queryParam("end", request.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (request.getUri() != null) {
            for (String uri : request.getUri()) {
                uriBuilder.queryParam("uris", uri);
            }
        }
        uriBuilder.queryParam("unique", request.getUnique());

        return WebClient.create()
                .get()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .bodyToMono(StatDto.class);
    }

    public Long getEventViews(String request) {
        String baseUrl = getStatsServiceUrl();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "/stats/event");
        uriBuilder.queryParam("uri", request);

        return WebClient.create()
                .get()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }
}
