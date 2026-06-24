package local.sop.sopinfo.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;



@SpringBootTest(classes = {SecurityTestApplication.class, SecurityTestController.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    UserDetailsServiceAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SecurityConfigIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldDenyAccessToInternalEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/internal/test"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldAllowHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
    
    @Test
    void shouldAllowInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
    }
    
    @Test
    void shouldDenyOtherActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldDenyPublicEndpoints() throws Exception {
        mockMvc.perform(get("/public/test"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldDenyRootEndpoint() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isForbidden());
    }

        @Test
        void shouldDenyUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get("/internal/number/123"))
                .andExpect(status().isForbidden());
        }

    @Test
    void shouldHandleInvalidPathVariable() throws Exception {
        mockMvc.perform(get("/internal/uuid/abc")
                .with(user("test").roles("INTERNAL"))
                .header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(status().isBadRequest()); 
    }
    
    @Test
    void shouldAllowValidInternalRequest() throws Exception {
        mockMvc.perform(get("/internal/uuid/123e4567-e89b-12d3-a456-426614174000")
                .with(user("test").roles("INTERNAL")))
            .andExpect(status().isOk());
    }

   
}
