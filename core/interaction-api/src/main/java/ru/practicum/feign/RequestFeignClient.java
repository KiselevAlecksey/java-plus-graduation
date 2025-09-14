package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/users/{userId}/requests")
public interface RequestFeignClient extends RequestControllerInternal {
}
