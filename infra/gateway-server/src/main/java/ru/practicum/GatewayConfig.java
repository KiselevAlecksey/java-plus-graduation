package ru.practicum;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GatewayConfig {
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                    .build());

            factory.addCircuitBreakerCustomizer(circuitBreaker -> {
                circuitBreaker.getEventPublisher()
                        .onStateTransition(event ->
                                log.info("CircuitBreaker {} changed from {} to {}",
                                        circuitBreaker.getName(),
                                        event.getStateTransition().getFromState(),
                                        event.getStateTransition().getToState())
                        );
            }, "shoppingCartCB", "warehouseCB");
        };
    }
}
