package local.sop.datawarehouse.login.saga.application.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import local.sop.datawarehouse.login.saga.application.api.dto.CreateAuditlogCmd;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginCmd;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.datawarehouse.login.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.login.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.exceptions.ErrorCode;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;



@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "bc.qualifier=login-saga",
    "security.enabled=false"
})
class LoginSagaApplicationServiceTest {

    @Mock
    private LoginPort loginPort;

    @Mock
    private ConsentPort consentPort;

    @Mock
    private AuditlogPort auditlogPort;

    @InjectMocks
    private LoginSagaApplicationService loginSagaApplicationService;

    private LoginCmd validLoginCmd;

    private Validator validator;

    @BeforeEach
    void setUp() {
        validLoginCmd = new LoginCmd(
            "testuser",
            "testpass",
            UUID.randomUUID(),
            ActorType.USER,
            Severity.INFO,
            "test-system",
            "test-service",
            "test-component",
            "test-data",
            "test-description");

            validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private LoginResult loginResult(UUID loginId, UUID personRef) {
        return new LoginResult(loginId, personRef, "testuser", "test-session-token",
            LocalDateTime.now(), LocalDateTime.now().plusHours(8));
    }

    @Test
    void login_shouldReturnLoginResult_whenSuccessful() {
        UUID loginId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        LoginResult expectedLoginResult = loginResult(loginId, personRef);
        when(loginPort.login("testuser", "testpass")).thenReturn(expectedLoginResult);
        when(consentPort.hasConsent(personRef)).thenReturn(true);
        when(auditlogPort.create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(UUID.randomUUID());

        LoginResult result = loginSagaApplicationService.login(validLoginCmd);

        assertNotNull(result);
        assertEquals(loginId, result.loginId());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(consentPort, times(1)).hasConsent(personRef);
        verify(auditlogPort, times(1)).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_shouldPropagateValidationException_whenLoginFailsValidation() {
        when(loginPort.login("testuser", "testpass")).thenThrow(new ValidationException("login.validation.failed", Map.of()));

        ValidationException exception = assertThrows(ValidationException.class, () -> loginSagaApplicationService.login(validLoginCmd));

        assertEquals("login.validation.failed", exception.getMessage());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(auditlogPort, never()).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_shouldPropagateDomainException_whenLoginFailsDomain() {
        when(loginPort.login("testuser", "testpass"))
                .thenThrow(new DomainException(ErrorCode.CONFLICT, "login.domain.failed", Map.of()));

        DomainException exception = assertThrows(DomainException.class, () -> loginSagaApplicationService.login(validLoginCmd));

        assertEquals("login.domain.failed", exception.getMessage());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(auditlogPort, never()).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_shouldTriggerCompensation_whenLoginFailsRuntime() {
        when(loginPort.login("testuser", "testpass")).thenThrow(new RuntimeException("Login service down"));

        ConflictException exception = assertThrows(ConflictException.class, () -> loginSagaApplicationService.login(validLoginCmd));

        assertEquals("login.notcreated", exception.getMessage());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(loginPort, never()).compensate(any(UUID.class), eq(LoginSagaApplicationService.class), eq(SagaOutcome.COMPENSATE));
        verify(auditlogPort, never()).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_shouldReturnLoginResult_whenAuditlogCreationSuccessful() {
        UUID loginId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        UUID auditlogId = UUID.randomUUID();
        LoginResult expectedLoginResult = loginResult(loginId, personRef);
        when(loginPort.login("testuser", "testpass")).thenReturn(expectedLoginResult);
        when(consentPort.hasConsent(personRef)).thenReturn(true);
        when(auditlogPort.create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(auditlogId);

        LoginResult result = loginSagaApplicationService.login(validLoginCmd);

        assertNotNull(result);
        assertEquals(loginId, result.loginId());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(consentPort, times(1)).hasConsent(personRef);
        verify(auditlogPort, times(1)).create(eq(validLoginCmd.actorRef()), any(ActorType.class), any(Severity.class),
                eq("test-system"), eq("test-service"), eq("test-component"), eq("test-data"), eq("test-description"));
    }

    @Test
    void login_shouldTriggerCompensation_whenAuditlogCreationFails() {
        UUID loginId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        LoginResult expectedLoginResult = loginResult(loginId, personRef);
        when(loginPort.login("testuser", "testpass")).thenReturn(expectedLoginResult);
        when(consentPort.hasConsent(personRef)).thenReturn(true);
        when(auditlogPort.create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Auditlog service down"));
        when(loginPort.compensate(loginId, LoginSagaApplicationService.class, SagaOutcome.COMPENSATE))
                .thenReturn(new ResponseCompensated(SagaOutcome.COMPENSATE, true));

        ConflictException exception = assertThrows(ConflictException.class, () -> loginSagaApplicationService.login(validLoginCmd));

        assertEquals("auditlog.notcreated", exception.getMessage());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(consentPort, times(1)).hasConsent(personRef);
        verify(auditlogPort, times(1)).create(eq(validLoginCmd.actorRef()), any(ActorType.class), any(Severity.class),
                eq("test-system"), eq("test-service"), eq("test-component"), eq("test-data"), eq("test-description"));
        verify(loginPort, times(1)).compensate(eq(loginId), eq(LoginSagaApplicationService.class), eq(SagaOutcome.COMPENSATE));
    }

    @Test
    void login_shouldTriggerCompensation_whenConsentNotGiven() {
        UUID loginId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        LoginResult expectedLoginResult = loginResult(loginId, personRef);
        when(loginPort.login("testuser", "testpass")).thenReturn(expectedLoginResult);
        when(consentPort.hasConsent(personRef)).thenReturn(false);
        when(loginPort.compensate(loginId, LoginSagaApplicationService.class, SagaOutcome.COMPENSATE))
                .thenReturn(new ResponseCompensated(SagaOutcome.COMPENSATE, true));

        ConflictException exception = assertThrows(ConflictException.class, () -> loginSagaApplicationService.login(validLoginCmd));

        assertEquals("consent.notgiven", exception.getMessage());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(consentPort, times(1)).hasConsent(personRef);
        verify(loginPort, times(1)).compensate(eq(loginId), eq(LoginSagaApplicationService.class), eq(SagaOutcome.COMPENSATE));
        verify(auditlogPort, never()).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void login_shouldTriggerCompensation_whenConsentCheckFails() {
        UUID loginId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        LoginResult expectedLoginResult = loginResult(loginId, personRef);
        when(loginPort.login("testuser", "testpass")).thenReturn(expectedLoginResult);
        when(consentPort.hasConsent(personRef)).thenThrow(new RuntimeException("Consent service down"));
        when(loginPort.compensate(loginId, LoginSagaApplicationService.class, SagaOutcome.COMPENSATE))
                .thenReturn(new ResponseCompensated(SagaOutcome.COMPENSATE, true));

        ConflictException exception = assertThrows(ConflictException.class, () -> loginSagaApplicationService.login(validLoginCmd));

        assertEquals("consent.check.failed", exception.getMessage());
        verify(loginPort, times(1)).login("testuser", "testpass");
        verify(consentPort, times(1)).hasConsent(personRef);
        verify(loginPort, times(1)).compensate(eq(loginId), eq(LoginSagaApplicationService.class), eq(SagaOutcome.COMPENSATE));
        verify(auditlogPort, never()).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString());
    }


    @Test
    void shouldPassValidation_whenCreateAuditlogCmdIsValid() {
            CreateAuditlogCmd cmd = new CreateAuditlogCmd(
                            UUID.randomUUID(),
                            ActorType.USER,
                            Severity.INFO,
                            "system",
                            "service",
                            "component",
                            "data",
                            "description");

            var violations = validator.validate(cmd);

            assertTrue(violations.isEmpty());
    }

    
}
