package local.sop.sopinfo.infrastructure.web.health;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.PostConstruct;

@Component
public class CommonWebHealthIndicator implements HealthIndicator {
    private Log log = org.apache.commons.logging.LogFactory.getLog(CommonWebHealthIndicator.class);
    @Autowired
    private ApplicationContext context;
    
    @PostConstruct
    public void init() {
        // Log alle beans annoteret med @RestControllerAdvice ved opstart
        Map<String, Object> adviceBeans = context.getBeansWithAnnotation(RestControllerAdvice.class);
        if (adviceBeans.isEmpty()) {
            log.info("No RestControllerAdvice beans found in context.");
        } else {
            log.info("Found " + adviceBeans.size() + " RestControllerAdvice beans:");
            adviceBeans.forEach((name, bean) -> {
                log.info("- " + name + ": " + bean.getClass().getName());
            });
        }
    }

    @Override
    public Health health() {
        int handlerCount = getHandlerCount();
        List<String> handlerNames = getHandlerNames();
        
        return Health.up()
            .withDetail("totalExceptionHandlers", handlerCount)
            .withDetail("handlerClasses", handlerNames)
            .withDetail("timestamp", Instant.now())
            .build();
    }
    
    public int getHandlerCount() {
        return (int) context.getBeansOfType(Object.class).entrySet().stream()
            .filter(entry -> context.findAnnotationOnBean(entry.getKey(), RestControllerAdvice.class) != null)
            .count();
    }
    
    public List<String> getHandlerNames() {
        return context.getBeansOfType(Object.class).entrySet().stream()
            .filter(entry -> context.findAnnotationOnBean(entry.getKey(), RestControllerAdvice.class) != null)
            .map(Map.Entry::getValue)
            .map(obj -> obj.getClass().getSimpleName())
            .collect(Collectors.toList());
    }
}
