package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/internal/admin/users")
public interface UserFeignClient extends UserAdminControllerInternal {
}
