package local.sop.sopinfo.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import local.sop.sopinfo.infrastructure.security.config.MtlsAutoConfiguration;
import local.sop.sopinfo.infrastructure.security.config.SecurityConfig;
import org.springframework.security.core.Authentication;

@SpringBootTest(
        // Brug den samme applikations‑klasse som i produktionen
        classes = SecurityTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")                // <-- indlæser application‑test.properties
@Import({SecurityConfig.class, MtlsAutoConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

class MtlsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    

    @Test
    @WithMockUser(username = "test", authorities = {"ROLE_INTERNAL"})
    void shouldAllowAccess_WithValidUser() throws Exception {
        // Når SSL‑bundle er indlæst, behøver vi ikke mocke SslBundles.
        mockMvc.perform(get("/internal/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Internal test endpoint"));
    }
     @Test
    @WithMockUser(username = "test", authorities = {"ROLE_INTERNAL"})
    void shouldAllowAccessToUuidEndpoint() throws Exception {
        String testUuid = "123e4567-e89b-12d3-a456-426614174000";
        mockMvc.perform(get("/internal/uuid/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(content().string("UUID: " + testUuid));
    }

@Test
void shouldDenyAccess_WhenUserNotInAllowedCallers() throws Exception {
    // Opret et Authentication‑objekt med et brugernavn, der ikke er tilladt
    Authentication notAllowed = new UsernamePasswordAuthenticationToken(
            "disallowed-service",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")));

    mockMvc.perform(get("/internal/test")
                    .with(authentication(notAllowed)))   // <-- brug authentication i stedet for x509
            .andExpect(status().isForbidden());
}


  @Test
    void shouldDenyAccess_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/internal/test"))
                .andExpect(status().isForbidden());
    }

      /* ---------- Actuator ---------- */

    @Test
    void shouldAllowAccessToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToOtherActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isForbidden());
    }
}