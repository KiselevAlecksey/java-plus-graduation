package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/internal/users/requests")
public interface RequestFeignClient extends RequestControllerInternal {
}
