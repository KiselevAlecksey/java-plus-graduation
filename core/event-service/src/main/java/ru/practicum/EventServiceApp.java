package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import ru.practicum.dto.converter.NewEventDtoToEventInteraction;

@SpringBootApplication
@EnableDiscoveryClient
@EnableRetry
@EnableFeignClients
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}
