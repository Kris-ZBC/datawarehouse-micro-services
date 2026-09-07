package local.sop.datawarehouse.gateway.common.handlers.login.application.interfaceweb;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
import java.time.LocalDateTime;
import java.util.UUID;
 
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
 
import com.fasterxml.jackson.databind.ObjectMapper;
 
import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.gateway.common.handlers.login.api.LoginDirectory;
import local.sop.datawarehouse.gateway.common.handlers.login.api.dto.LoginRequest;
import local.sop.datawarehouse.gateway.common.handlers.login.application.config.CookieProps;
import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort.LoginResult;
 
@WebMvcTest(controllers = LoginController.class)
@Import(EndpointExceptionHandler.class)
@EnableConfigurationProperties(CookieProps.class)
@TestPropertySource(properties = {
        "sop.session-cookie.name=SOP_SESSION",
        "sop.session-cookie.domain=.sop.local"
})
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LoginControllerTest {
 
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objMapper;
    @MockitoBean private LoginDirectory loginDirectory;
 
    private MvcResult startAsync(String uri, Object body) throws Exception {
        return mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body != null ? objMapper.writeValueAsString(body) : ""))
                .andExpect(request().asyncStarted())
                .andReturn();
    }
 
    @Nested
    class Login {
 
        @Test
        void shouldReturn200_withCookieSetAndNoTokenInBody() throws Exception {
            LoginResult result = new LoginResult(UUID.randomUUID(), UUID.randomUUID(), "nick",
                    "INSTRUCTOR", "raw-session-token", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
            when(loginDirectory.login(any())).thenReturn(result);
 
            MvcResult started = startAsync("/api/v1/common/login", new LoginRequest("nick", "pw"));
 
            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("nick"))
                    .andExpect(jsonPath("$.role").value("INSTRUCTOR"))
                    .andExpect(jsonPath("$.sessionToken").doesNotExist())
                    .andExpect(cookie().value("SOP_SESSION", "raw-session-token"))
                    .andExpect(cookie().httpOnly("SOP_SESSION", true))
                    .andExpect(cookie().secure("SOP_SESSION", true))
                    .andExpect(cookie().path("SOP_SESSION", "/"))
                    .andExpect(cookie().domain("SOP_SESSION", ".sop.local"))
                    .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")));
        }
 
        @Test
        void shouldReturn400_whenCredentialsInvalid() throws Exception {
            when(loginDirectory.login(any()))
                    .thenThrow(new ValidationException("login.credentials.invalid", java.util.Map.of()));
 
            MvcResult started = startAsync("/api/v1/common/login", new LoginRequest("nick", "wrong"));
 
            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void shouldReturn400_whenUsernameBlank() throws Exception {
            mockMvc.perform(post("/api/v1/common/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objMapper.writeValueAsString(new LoginRequest("", "pw"))))
                    .andExpect(status().isBadRequest());
        }
    }
 
    @Nested
    class Logout {
 
        @Test
        void shouldReturn204_andClearCookie_whenSessionCookiePresent() throws Exception {
            MvcResult started = mockMvc.perform(post("/api/v1/common/logout")
                            .cookie(new jakarta.servlet.http.Cookie("SOP_SESSION", "raw-session-token")))
                    .andExpect(request().asyncStarted())
                    .andReturn();
 
            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isNoContent())
                    .andExpect(cookie().maxAge("SOP_SESSION", 0));
 
            verify(loginDirectory).logout("raw-session-token");
        }
 
        @Test
        void shouldReturn204_andSkipLogoutCall_whenNoCookiePresent() throws Exception {
            MvcResult started = mockMvc.perform(post("/api/v1/common/logout"))
                    .andExpect(request().asyncStarted())
                    .andReturn();
 
            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isNoContent());
 
            verify(loginDirectory, never()).logout(any());
        }
    }
}
