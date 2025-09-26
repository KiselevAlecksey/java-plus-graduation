package ru.practicum.aop.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.practicum.aop.config.RequestScopeObject;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RestLoggingAspect {
    private static final String PROCESS_START_LOGGER_PATTERN =
            "==> [{}] {}.{}.{} started. {} request to url {} ";
    private static final String PROCESS_START_WITH_BODY_LOGGER_PATTERN =
            "==> [{}] {}.{}.{} started. {} request to url {}. {}";
    private static final String PROCESS_END_LOGGER_PATTERN =
            "<== [{}] {}.{}.{} completed. {} request to url {}nExecution time :: {} ms";
    private static final String PROCESS_END_WITH_BODY_LOGGER_PATTERN =
            "<== [{}] {}.{}.{} completed. {} request to url {}. {}nExecution time :: {} ms";

    @Value("${spring.application.name}")
    private String appName;

    public static final String X_REQUEST_ID_HEADER = "x-request-id";

    private final RequestScopeObject requestScopeObject;

    @Around("@annotation(RestLogging)")
    public Object toLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        ObjectMapper mapper = new ObjectMapper();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        HttpServletRequest request =
                ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
                        .getRequestAttributes()))
                        .getRequest();

        if (StringUtils.isBlank(requestScopeObject.getRequestId())) {

            String requestId = request.getHeader(X_REQUEST_ID_HEADER);
            if (StringUtils.isBlank(requestId)) {
                requestId = UUID.randomUUID().toString().toUpperCase().replace("-", "");
            }
            requestScopeObject.setRequestId(requestId);
        }

        if (StringUtils.isBlank(requestScopeObject.getOperationId())) {
            String operationId = UUID.randomUUID().toString().toUpperCase();
            requestScopeObject.setOperationId(operationId);
        }
        String rqUrl = Optional.ofNullable(request.getQueryString()).isPresent() ?
                request.getRequestURL().toString() + "?" +
                        request.getQueryString() : request.getRequestURL().toString();

        if (!log.isDebugEnabled()) {
            log.info(PROCESS_START_LOGGER_PATTERN, requestScopeObject.getRequestId(),
                    appName, className, methodName, request.getMethod(), rqUrl);

        } else {
            Object[] args = proceedingJoinPoint.getArgs();
            boolean hasBody = args != null && args.length > 0;

            if (hasBody) {

                JsonNode node = mapper.valueToTree(proceedingJoinPoint.getArgs());
                log.debug(PROCESS_START_WITH_BODY_LOGGER_PATTERN,
                        requestScopeObject.getRequestId(),
                        appName, className, methodName, request.getMethod(),
                        rqUrl, node.toPrettyString());

            } else {

                log.debug(PROCESS_START_LOGGER_PATTERN, requestScopeObject.getRequestId(),
                        appName, className, methodName,
                        request.getMethod(), rqUrl);
            }
        }
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();

        if (!log.isDebugEnabled()) {
            log.info(PROCESS_END_LOGGER_PATTERN,
                    requestScopeObject.getRequestId(), appName, className, methodName,
                    request.getMethod(), rqUrl, stopWatch.getTotalTimeMillis());
        } else {
            if (!"DELETE".equalsIgnoreCase(request.getMethod()) && Objects.nonNull(result)) {
                log.debug(PROCESS_END_WITH_BODY_LOGGER_PATTERN,
                        requestScopeObject.getRequestId(), appName, className, methodName,
                        request.getMethod(), rqUrl, mapper.valueToTree(result).toPrettyString(),
                        stopWatch.getTotalTimeMillis());
            } else {
                log.debug(PROCESS_END_LOGGER_PATTERN,
                        requestScopeObject.getRequestId(), appName, className, methodName,
                        request.getMethod(), rqUrl, stopWatch.getTotalTimeMillis());
            }
        }
        return result;
    }
}
