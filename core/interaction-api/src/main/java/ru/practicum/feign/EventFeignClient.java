package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", path = "/internal/users")
public interface EventFeignClient extends UserEventControllerInternal {
}
