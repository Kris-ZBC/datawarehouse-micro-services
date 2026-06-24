package local.sop.sopinfo.interfaceadapters.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import local.sop.sopinfo.infrastructure.web.health.CommonWebHealthIndicator;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ExceptionHandlerHealthIndicatorTest {
    @Autowired
    private CommonWebHealthIndicator healthIndicator;

    @Test
    void health_ShouldReturnCorrectHandlerInfo() {
        Health health = healthIndicator.health();
        
        // Verify health status
        assertEquals(Status.UP, health.getStatus());
        
        // Verify structure
        assertNotNull(health.getDetails());
        assertTrue(health.getDetails().containsKey("totalExceptionHandlers"));
        assertTrue(health.getDetails().containsKey("handlerClasses"));
        assertTrue(health.getDetails().containsKey("timestamp"));
        
        // Verify count
        int handlerCount = (int) health.getDetails().get("totalExceptionHandlers");
        assertTrue(handlerCount > 0);
        
        // Verify handler names list
        @SuppressWarnings("unchecked")
        List<String> handlerNames = (List<String>) health.getDetails().get("handlerClasses");
        assertEquals(handlerCount, handlerNames.size());
        assertTrue(handlerNames.stream().allMatch(name -> !name.isEmpty()));
        
        // Verify timestamp
        assertInstanceOf(Instant.class, health.getDetails().get("timestamp"));
    }

    @Test
    void health_ShouldIncludeAllRestControllerAdviceBeans() {
        Health health = healthIndicator.health();
        
        @SuppressWarnings("unchecked")
        List<String> handlerNames = (List<String>) health.getDetails().get("handlerClasses");
        
        // Test at din exception handler er inkluderet
        assertTrue(handlerNames.contains("EndpointExceptionHandler")); // Udskift med dit class name
    }

    @Test
    void getHandlerCount_ShouldBeAccurate() {
        int count = healthIndicator.getHandlerCount();
        List<String> names = healthIndicator.getHandlerNames();
        
        assertEquals(count, names.size());
    }
}
