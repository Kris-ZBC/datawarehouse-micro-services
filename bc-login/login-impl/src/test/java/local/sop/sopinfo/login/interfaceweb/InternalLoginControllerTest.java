package local.sop.sopinfo.login.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
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
import local.sop.sopinfo.login.application.api.LoginDirectory;
import local.sop.sopinfo.login.application.api.dto.*;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@WebMvcTest(controllers = InternalLoginController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InternalLoginControllerTest {

    private static final String BASE = "/internal/logins";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private LoginDirectory directory;
    @Autowired private ObjectMapper objMapper;

    // ── createLogin ────────────────────────────────────────────────────────────

    @Nested
    class CreateLoginTests {

        @Test
        void shouldReturn201_withLocationHeader() throws Exception {
            UUID id = UUID.randomUUID();
            when(directory.createLogin(any()))
                .thenReturn(new CreatedLoginResult(id, "Password5!", LocalDateTime.now()));

            mockMvc.perform(post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new CreateLoginCmd(UUID.randomUUID(), "nick579a@zbc.dk", "ACTIVATED"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));

            verify(directory, times(1)).createLogin(any());
        }

        @Test
        void shouldReturn400_whenUsernameIsInvalid() throws Exception {
            when(directory.createLogin(any()))
                .thenThrow(new ValidationException("login.username.invalid",
                    Map.of("field", "username")));

            mockMvc.perform(post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new CreateLoginCmd(UUID.randomUUID(), "invalid", "ACTIVATED"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("login.username.invalid"));
        }

        @Test
        void shouldReturn400_whenStatusIsInvalid() throws Exception {
            when(directory.createLogin(any()))
                .thenThrow(new ValidationException("login.status.invalid",
                    Map.of("field", "status")));

            mockMvc.perform(post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new CreateLoginCmd(UUID.randomUUID(), "nick579a@zbc.dk", "INVALID"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("login.status.invalid"));
        }

        @Test
        void shouldReturn500_whenServiceThrowsUnexpectedException() throws Exception {
            when(directory.createLogin(any())).thenThrow(new RuntimeException("db crash"));

            mockMvc.perform(post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new CreateLoginCmd(UUID.randomUUID(), "nick579a@zbc.dk", "ACTIVATED"))))
                .andExpect(status().isInternalServerError());
        }
    }

    // ── login ──────────────────────────────────────────────────────────────────

    @Nested
    class LoginTests {

        @Test
        void shouldReturn200_withSessionToken_whenCredentialsAreValid() throws Exception {
            UUID loginId = UUID.randomUUID();
            String token = UUID.randomUUID().toString();

            when(directory.login(any(LoginCmd.class)))
                .thenReturn(new LoginResult(loginId, UUID.randomUUID(),
                    "nick579a@zbc.dk", token,
                    LocalDateTime.now(), LocalDateTime.now().plusHours(8)));

            mockMvc.perform(post(BASE + "/sessions/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new LoginCmd("nick579a@zbc.dk", "Password5!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value(token))
                .andExpect(jsonPath("$.username").value("nick579a@zbc.dk"));
        }

        @Test
        void shouldNotSetCookie_sessionTokenIsInResponseBodyOnly() throws Exception {
            when(directory.login(any(LoginCmd.class)))
                .thenReturn(new LoginResult(UUID.randomUUID(), UUID.randomUUID(),
                    "nick579a@zbc.dk", UUID.randomUUID().toString(),
                    LocalDateTime.now(), LocalDateTime.now().plusHours(8)));

            mockMvc.perform(post(BASE + "/sessions/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new LoginCmd("nick579a@zbc.dk", "Password5!"))))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("X-Session-Token"));
        }

        @Test
        void shouldReturn400_whenCredentialsAreInvalid() throws Exception {
            when(directory.login(any()))
                .thenThrow(new ValidationException("login.credentials.invalid",
                    Map.of("username", "nick579a@zbc.dk")));

            mockMvc.perform(post(BASE + "/sessions/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new LoginCmd("nick579a@zbc.dk", "WrongPassword5!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("login.credentials.invalid"));
        }

        @Test
        void shouldReturn400_whenLoginIsDeactivated() throws Exception {
            when(directory.login(any()))
                .thenThrow(new ValidationException("login.deactivated",
                    Map.of("username", "nick579a@zbc.dk")));

            mockMvc.perform(post(BASE + "/sessions/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new LoginCmd("nick579a@zbc.dk", "Password5!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("login.deactivated"));
        }
    }

    // ── logout ─────────────────────────────────────────────────────────────────

    @Nested
    class LogoutTests {

        @Test
        void shouldReturn204_whenLogoutSuccessful() throws Exception {
            mockMvc.perform(post(BASE + "/sessions/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new LogoutCmd(UUID.randomUUID().toString()))))
                .andExpect(status().isNoContent());

            verify(directory).logout(any(LogoutCmd.class));
        }

        @Test
        void shouldReturn404_whenSessionNotFound() throws Exception {
            doThrow(new NotFoundException("session.not.found", Map.of("key", "token")))
                .when(directory).logout(any());

            mockMvc.perform(post(BASE + "/sessions/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new LogoutCmd(UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
        }
    }

    // ── validateSession ────────────────────────────────────────────────────────

    @Nested
    class ValidateSessionTests {

        @Test
        void shouldReturn200_withValidationResult_whenSessionIsValid() throws Exception {
            UUID loginId   = UUID.randomUUID();
            UUID personRef = UUID.randomUUID();
            String token   = UUID.randomUUID().toString();

            when(directory.validateSession(any(ValidateSessionCmd.class)))
                .thenReturn(new SessionValidationResult(
                    true, loginId, personRef,
                    "nick579a@zbc.dk", LocalDateTime.now().plusHours(8)));

            mockMvc.perform(post(BASE + "/sessions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(new ValidateSessionCmd(token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.loginId").value(loginId.toString()))
                .andExpect(jsonPath("$.personRef").value(personRef.toString()))
                .andExpect(jsonPath("$.username").value("nick579a@zbc.dk"))
                .andExpect(jsonPath("$.expiresAt").exists());
        }

        @Test
        void shouldReturn401_whenSessionIsInvalid() throws Exception {
            when(directory.validateSession(any(ValidateSessionCmd.class)))
                .thenReturn(SessionValidationResult.invalid());

            mockMvc.perform(post(BASE + "/sessions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new ValidateSessionCmd(UUID.randomUUID().toString()))))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenSessionIsExpired() throws Exception {
            when(directory.validateSession(any(ValidateSessionCmd.class)))
                .thenReturn(SessionValidationResult.invalid());

            mockMvc.perform(post(BASE + "/sessions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new ValidateSessionCmd(UUID.randomUUID().toString()))))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn400_whenTokenIsMissing() throws Exception {
            mockMvc.perform(post(BASE + "/sessions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new ValidateSessionCmd(null))))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenTokenIsBlank() throws Exception {
            mockMvc.perform(post(BASE + "/sessions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new ValidateSessionCmd("   "))))
                .andExpect(status().isBadRequest());
        }
    }

    // ── compensate ─────────────────────────────────────────────────────────────

    @Nested
    class CompensateTests {

        @Test
        void shouldReturn200_whenCompensationSucceeds() throws Exception {
            UUID id = UUID.randomUUID();
            when(directory.compensate(any(), any(), any()))
                .thenReturn(new ResponseCompensated(SagaOutcome.COMPENSATED, true));

            mockMvc.perform(post(BASE + "/compensate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new InternalLoginController.CompensateLoginCmd(
                            id, SagaOutcome.COMPENSATED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sagaState").value("COMPENSATED"))
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void shouldReturn200_whenAlreadyCompensated() throws Exception {
            UUID id = UUID.randomUUID();
            when(directory.compensate(any(), any(), any()))
                .thenReturn(new ResponseCompensated(SagaOutcome.IDEMPOTENT, true));

            mockMvc.perform(post(BASE + "/compensate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objMapper.writeValueAsString(
                        new InternalLoginController.CompensateLoginCmd(
                            id, SagaOutcome.COMPENSATED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sagaState").value("IDEMPOTENT"));
        }
    }
}