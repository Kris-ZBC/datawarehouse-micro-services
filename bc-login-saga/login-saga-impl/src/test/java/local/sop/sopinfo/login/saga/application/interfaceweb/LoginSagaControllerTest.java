package local.sop.sopinfo.login.saga.application.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.sopinfo.login.saga.application.api.LoginSagaDirectory;
import local.sop.sopinfo.login.saga.application.api.dto.LoginCmd;
import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

@WebMvcTest(LoginSagaController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LoginSagaControllerTest {

    private static final String URL = "/internal/saga/logins/sessions/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginSagaDirectory directory;

    private LoginCmd validCmd;
    private LoginResult loginResult;

    @BeforeEach
    void setUp() {
        validCmd = new LoginCmd(
                "testuser",
                "testpass",
                UUID.randomUUID(),
                ActorType.USER,
                Severity.INFO,
                "originSystem",
                "originService",
                "originComponent",
                "data",
                "description");
        UUID loginId = UUID.randomUUID();
        loginResult = new LoginResult(loginId, UUID.randomUUID(), "testuser",
            "test-token", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
    }

    @Test
    void login_shouldReturn200AndPayload_whenRequestIsValid() throws Exception {
        when(directory.login(any(LoginCmd.class))).thenReturn(loginResult);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(loginResult.loginId().toString()));
    }

    @Test
    void login_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
        when(directory.login(any(LoginCmd.class))).thenReturn(loginResult);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCmd)));

        verify(directory).login(any(LoginCmd.class));
    }

    @Test
    void login_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400_whenActorRefIsNull() throws Exception {
        LoginCmd invalidCmd = new LoginCmd(
                validCmd.username(),
                validCmd.password(),
                null,
                validCmd.actorType(),
                validCmd.severity(),
                validCmd.originSystem(),
                validCmd.originService(),
                validCmd.originComponent(),
                validCmd.data(),
                validCmd.description());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCmd)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400_whenOriginSystemIsBlank() throws Exception {
        LoginCmd invalidCmd = new LoginCmd(
                validCmd.username(),
                validCmd.password(),
                validCmd.actorRef(),
                validCmd.actorType(),
                validCmd.severity(),
                "   ",
                validCmd.originService(),
                validCmd.originComponent(),
                validCmd.data(),
                validCmd.description());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCmd)))
                .andExpect(status().isBadRequest());
    }
}
