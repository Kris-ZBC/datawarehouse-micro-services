package local.sop.sopinfo.education.saga.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.education.saga.application.api.dto.CreateAuditlog;
import local.sop.sopinfo.education.saga.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.saga.application.api.dto.CreateEducationInstructorCmd;
import local.sop.sopinfo.education.saga.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.education.saga.application.api.dto.EducationResponse;
import local.sop.sopinfo.education.saga.application.api.dto.UpdateEducationCategoryCmd;
import local.sop.sopinfo.education.saga.application.api.dto.UpdateEducationNameCmd;
import local.sop.sopinfo.education.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.education.saga.application.ports.out.education.EducationPort;
import local.sop.sopinfo.education.saga.application.ports.out.educationinstructor.EducationInstructorPort;
import local.sop.sopinfo.education.saga.application.ports.out.saga.EducationSagaStatePort;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;

@ExtendWith(MockitoExtension.class)
public class EducationSagaApplicationServiceTest {

    @Mock
    private AuditlogPort auditlogPort;
    
    @Mock
    private EducationPort educationPort;
    
    @Mock
    private EducationSagaStatePort sagaLock;

    @Mock
    private EducationInstructorPort educationInstructorPort;

    private EducationSagaApplicationService service;

    private UUID actorRef;
    private UUID sessionId;
    private UUID auditlogId;
    private UUID educationId;
    private UUID instructorId;
    private CompositeKey educationInstructorId;

    private CreateAuditlog auditCmd;
    private CreateEducationCmd createCmd;
    private UpdateEducationCategoryCmd updateCategoryCmd;
    private UpdateEducationNameCmd updateNameCmd;
    private EducationResponse educationResponse;
    private EducationInstructorResponse educationInstructorResponse;
    private CreateEducationInstructorCmd createEducationInstructorCmd;

    // ── Session lock ───────────────────────────────────────────────────────────

    @BeforeEach
    void setup() {
        service = new EducationSagaApplicationService
        (educationPort, educationInstructorPort, auditlogPort, sagaLock);
        sessionId = UUID.randomUUID();
        educationId = UUID.randomUUID();
        instructorId = UUID.randomUUID();
        educationInstructorId = new CompositeKey(educationId, instructorId);
        auditlogId = UUID.randomUUID();
        actorRef = UUID.randomUUID();

        createCmd = new CreateEducationCmd(sessionId, "Java 101", "Intro to Java", actorRef,
                ActorType.USER, Severity.INFO, "originSystem", "originService", "originComponent", "data",
                "description");
        
        educationResponse = new EducationResponse(educationId, "Java 101", "Intro to Java", true);

        updateCategoryCmd = new UpdateEducationCategoryCmd(
            sessionId,
            "Programming",
            actorRef,
            ActorType.USER,
            Severity.INFO,
            "originSystem",
            "originService",
            "originComponent",
            "data",
            "description"
        );

        updateNameCmd = new UpdateEducationNameCmd(
            sessionId,
            "Java Advanced",
            actorRef,
            ActorType.USER,
            Severity.INFO,
            "originSystem",
            "originService",
            "originComponent",
            "data",
            "description"
        );

        auditCmd = new CreateAuditlog(
            sessionId,
            actorRef,
            ActorType.USER,
            Severity.INFO,
            "originSystem",
            "originService",
            "originComponent",
            "data",
            "description"
        );

        createEducationInstructorCmd = new CreateEducationInstructorCmd(
            sessionId,
            educationInstructorId,
            educationId,
            instructorId,
            actorRef,
            ActorType.USER,
            Severity.INFO,
            "originSystem",
            "originService",
            "originComponent",
            "data",
            "description"
        );

            educationInstructorResponse = new EducationInstructorResponse(
                            educationInstructorId,
                            Instant.now(),
                            true);
    }

    @Nested
    class GeneralTests {
        @Test
        void shouldAlwaysReleaseLockEvenWhenUnexpectedExceptionOccurs() {

                when(sagaLock.tryLock(any())).thenReturn(true);

                assertThrows(
                                RuntimeException.class,
                                () -> service.createEducation(createCmd));

                verify(sagaLock).release(sessionId);
                verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
    }
    @Nested
    class SessionLock {
        @Test
        void shouldAcquireLockSuccessfully() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.createEducation(any()))
                    .thenReturn(educationId);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            assertDoesNotThrow(() -> service.createEducation(createCmd));

            verify(sagaLock).tryLock(any());
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldThrowConflictWhenLockAlreadyExists() {

            when(sagaLock.tryLock(any())).thenReturn(false);

            assertThrows(
                    ConflictException.class,
                    () -> service.createEducation(createCmd));

            verify(sagaLock).tryLock(any());
            verify(sagaLock, never()).release(any());
        }

        @Test
        void shouldAlwaysReleaseLockEvenWhenExceptionOccurs() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            assertThrows(
                    RuntimeException.class,
                    () -> service.createEducation(createCmd));

            verify(sagaLock).release(sessionId);
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
    }
    
    @Nested
    class CreateEducation {
        @Test
        void shouldCreateEducationSuccessfully() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.createEducation(createCmd))
                    .thenReturn(educationId);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationResponse result = service.createEducation(createCmd);

            assertNotNull(result);
            assertEquals(educationId, result.id());

            verify(sagaLock).tryLock(any());
            verify(sagaLock).release(sessionId);
            verify(sagaLock, never()).updateStatus(any(), eq(SagaStatus.COMPENSATING));
        }

        @Test
        void shouldCompensateWhenEducationMissingAfterCreate() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.createEducation(createCmd))
                    .thenReturn(educationId);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.createEducation(createCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldCompensateWhenAuditCreationFails() {

            when(sagaLock.tryLock(any())).thenReturn(true);


            when(educationPort.createEducation(createCmd))
                    .thenReturn(educationId);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenThrow(new RuntimeException());

            assertThrows(
                    ConflictException.class,
                    () -> service.createEducation(createCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldCompensateWhenAuditNotFoundAfterCreation() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.createEducation(createCmd))
                    .thenReturn(educationId);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.createEducation(createCmd));

            verify(auditlogPort)
                    .compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
    }
    
    @Nested
    class UpdateEducationName {

        @Test
        void shouldUpdateEducationNameSuccessfully() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.updateEducationName(educationId, updateNameCmd.name()))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationResponse result = service.updateEducationName(educationId, updateNameCmd);

            assertNotNull(result);
            assertEquals(educationId, result.id());
            verify(educationPort).updateEducationName(educationId, updateNameCmd.name());
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldThrowWhenEducationNotFound() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationName(educationId, updateNameCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenAuditCreationFails() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.updateEducationName(any(), anyString()))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenThrow(new RuntimeException());

            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationName(educationId, updateNameCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldCompensateWhenAuditNotFoundAfterCreation() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.updateEducationName(any(), anyString()))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationName(educationId, updateNameCmd));

            verify(auditlogPort)
                    .compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
        
        @Test
        void shouldCompensateWhenEducationMissingAfterNameUpdate() {

            when(sagaLock.tryLock(any())).thenReturn(true);
    
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
    
            // updateEducationName returns invalid response → triggers the warn + ConflictException
            when(educationPort.updateEducationName(educationId, updateNameCmd.name()))
                    .thenReturn(new EducationResponse(null, null, null, false));
    
            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationName(educationId, updateNameCmd)
            );
    
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldCompensateOnRuntimeExceptionDuringUpdateEducationName() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
            // simulate runtime exception inside updateEducationName
            when(educationPort.updateEducationName(any(), any()))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.updateEducationName(educationId, updateNameCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

    }
    
    @Nested
    class UpdateEducationCategory {

        @Test
        void shouldUpdateEducationCategorySuccessfully() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.updateEducationCategory(educationId, updateCategoryCmd.category()))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationResponse result = service.updateEducationCategory(educationId, updateCategoryCmd);

            assertNotNull(result);
            assertEquals(educationId, result.id());
            verify(educationPort).updateEducationCategory(educationId, updateCategoryCmd.category());
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldThrowWhenEducationNotFound() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationCategory(educationId, updateCategoryCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenAuditCreationFails() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.updateEducationCategory(any(), anyString()))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenThrow(new RuntimeException());

            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationCategory(educationId, updateCategoryCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldCompensateWhenAuditNotFoundAfterCreation() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                .thenReturn(educationResponse);

            when(educationPort.updateEducationCategory(any(), anyString()))
                .thenReturn(educationResponse);

            when(auditlogPort.create(
                any(), any(), any(),
                anyString(), anyString(), anyString(),
                anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                .thenReturn(null);

            assertThrows(
                            ConflictException.class,
                () -> service.updateEducationCategory(educationId, updateCategoryCmd));

            verify(auditlogPort)
                            .compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
        
        @Test
        void shouldCompensateWhenEducationMissingAfterUpdateCategory() {

            when(sagaLock.tryLock(any())).thenReturn(true);
    
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
    
            // updateEducationCategory returns invalid response → triggers the warn + ConflictException
            when(educationPort.updateEducationCategory(educationId, updateCategoryCmd.category()))
                    .thenReturn(new EducationResponse(null, null, null, false));
    
            assertThrows(
                    ConflictException.class,
                    () -> service.updateEducationCategory(educationId, updateCategoryCmd)
            );
    
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldCompensateOnRuntimeExceptionDuringUpdateEducationCategory() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
            // simulate runtime exception inside updateEducationCategory
            when(educationPort.updateEducationCategory(any(), any()))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.updateEducationCategory(educationId, updateCategoryCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
    }
    
    @Nested
    class ActivateDeactivateEducation {

        @Test
        void shouldActivateEducationSuccessfully() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.activateEducation(educationId))
                    .thenReturn(new EducationResponse(
                            educationId,
                            "Java 101",
                            "Intro to Java",
                            true));

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationResponse result = service.activateEducation(educationId, auditCmd);

            assertNotNull(result);
            assertTrue(result.active());
            verify(educationPort).activateEducation(educationId);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldDeactivateEducationSuccessfully() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.deactivateEducation(educationId))
                    .thenReturn(new EducationResponse(
                            educationId,
                            "Java 101",
                            "Intro to Java",
                            false));

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(auditlogId);

            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationResponse result = service.deactivateEducation(educationId, auditCmd);

            assertNotNull(result);
            assertFalse(result.active());
            verify(educationPort).deactivateEducation(educationId);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldThrowWhenEducationNotFoundOnActivate() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.activateEducation(educationId, auditCmd));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldThrowWhenEducationNotFoundOnDeactivate() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.deactivateEducation(educationId, auditCmd));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldThrowWhenActivateAuditFails() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);

            when(educationPort.activateEducation(educationId))
                    .thenReturn(educationResponse);

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenThrow(new RuntimeException());

            assertThrows(
                ConflictException.class,
                () -> service.activateEducation(educationId, auditCmd)
                );

            verify(sagaLock).release(sessionId);
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldThrowWhenDeactivateAuditFails() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationPort.findEducationById(educationId))
                .thenReturn(educationResponse);
            
            when(educationPort.deactivateEducation(educationId))
                    .thenReturn(new EducationResponse(
                            educationId,
                            "Java 101",
                            "Intro to Java",
                            false));

            when(auditlogPort.create(
                    any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenThrow(new RuntimeException());

            assertThrows(
                ConflictException.class,
                () -> service.deactivateEducation(educationId, auditCmd)
            );

            verify(sagaLock).release(sessionId);
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
        
        @Test
        void shouldCompensateWhenEducationMissingAfterDeactivation() {

            when(sagaLock.tryLock(any())).thenReturn(true);
    
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
    
            // deactivateEducation returns invalid response → triggers the warn + ConflictException
            when(educationPort.deactivateEducation(educationId))
                    .thenReturn(new EducationResponse(null, null, null, false));
    
            assertThrows(
                    ConflictException.class,
                    () -> service.deactivateEducation(educationId, auditCmd)
            );
    
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldCompensateWhenEducationMissingAfterActivation() {

            when(sagaLock.tryLock(any())).thenReturn(true);
    
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
    
            // activateEducation returns invalid response → triggers the warn + ConflictException
            when(educationPort.activateEducation(educationId))
                    .thenReturn(new EducationResponse(null, null, null, false));
    
            assertThrows(
                    ConflictException.class,
                    () -> service.activateEducation(educationId, auditCmd)
            );
    
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldCompensateOnRuntimeExceptionDuringUpdateEducationActivate() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
            // simulate runtime exception inside ActivateEducation
            when(educationPort.activateEducation(educationId))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.activateEducation(educationId, auditCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldCompensateOnRuntimeExceptionDuringUpdateEducationDeactivate() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            // validateEducation passes
            when(educationPort.findEducationById(educationId))
                    .thenReturn(educationResponse);
            // simulate runtime exception inside deactivateEducation
            when(educationPort.deactivateEducation(educationId))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.deactivateEducation(educationId, auditCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
    }

    @Nested
    class CreateEducationInstructorTests {

        @Test
        void shouldCreateEducationInstructorSuccessfully() {

                when(sagaLock.tryLock(any())).thenReturn(true);

                when(educationInstructorPort.createEducationInstructor(any()))
                                .thenReturn(educationInstructorResponse);

                when(auditlogPort.create(any(), any(), any(),
                                anyString(), anyString(), anyString(),
                                anyString(), anyString()))
                                .thenReturn(auditlogId);

                when(auditlogPort.findById(auditlogId))
                                .thenReturn(auditlogId);

                EducationInstructorResponse result = service.createEducationInstructor(createEducationInstructorCmd);

                assertNotNull(result);
                assertEquals(educationInstructorId, result.id());

                verify(educationInstructorPort).createEducationInstructor(createEducationInstructorCmd);
                verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldThrowWhenResponseIsNull() {

            when(sagaLock.tryLock(any())).thenReturn(true);

            when(educationInstructorPort.createEducationInstructor(any()))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.createEducationInstructor(createEducationInstructorCmd)
            );

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldThrowWhenAuditFails() {

        when(sagaLock.tryLock(any())).thenReturn(true);

        when(educationInstructorPort.createEducationInstructor(any()))
                .thenReturn(educationInstructorResponse);

        when(auditlogPort.create(any(), any(), any(),
                anyString(), anyString(), anyString(),
                anyString(), anyString()))
                .thenThrow(new RuntimeException());

        assertThrows(
                ConflictException.class,
                () -> service.createEducationInstructor(createEducationInstructorCmd)
        );

        verify(sagaLock).release(sessionId);
        verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
        
        @Test
        void shouldCompensateOnRuntimeExceptionDuringCreateEducationInstructor() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            // simulate runtime exception inside createEducationInstructor
            when(educationInstructorPort.createEducationInstructor(any()))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.createEducationInstructor(createEducationInstructorCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
    }

    @Nested
    class ActivateEducationInstructorTests {

        @Test
        void shouldActivateEducationInstructorSuccessfully() {

            EducationInstructorResponse activatedResponse =
                    new EducationInstructorResponse(educationInstructorId, Instant.now(), true);

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.activateEducationInstructor(educationInstructorId))
                    .thenReturn(activatedResponse);
            when(auditlogPort.create(any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString()))
                    .thenReturn(auditlogId);
            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationInstructorResponse result =
                    service.activateEducationInstructor(educationInstructorId, auditCmd);

            assertNotNull(result);
            assertEquals(educationInstructorId, result.id());
            assertTrue(result.isActive());

            verify(educationInstructorPort).findById(educationInstructorId);
            verify(educationInstructorPort).activateEducationInstructor(educationInstructorId);
            verify(sagaLock).release(sessionId);
            verify(sagaLock, never()).updateStatus(any(), eq(SagaStatus.COMPENSATING));
        }

        @Test
        void shouldThrowWhenEducationInstructorNotFoundOnActivate() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ConflictException.class,
                    () -> service.activateEducationInstructor(educationInstructorId, auditCmd));

            verify(educationInstructorPort, never()).activateEducationInstructor(any());
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenActivateResponseIsNull() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.activateEducationInstructor(educationInstructorId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.activateEducationInstructor(educationInstructorId, auditCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenActivateResponseIsInactive() {

            EducationInstructorResponse inactiveResponse =
                    new EducationInstructorResponse(educationInstructorId, Instant.now(), false);

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.activateEducationInstructor(educationInstructorId))
                    .thenReturn(inactiveResponse);

            assertThrows(
                    ConflictException.class,
                    () -> service.activateEducationInstructor(educationInstructorId, auditCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenActivateAuditCreationFails() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.activateEducationInstructor(educationInstructorId))
                    .thenReturn(educationInstructorResponse);
            when(auditlogPort.create(any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString()))
                    .thenThrow(new RuntimeException());

            assertThrows(
                    ConflictException.class,
                    () -> service.activateEducationInstructor(educationInstructorId, auditCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
        
        @Test
        void shouldCompensateOnRuntimeExceptionDuringActivateEducationInstructor() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));

            // simulate runtime exception inside activateEducationInstructor
            when(educationInstructorPort.activateEducationInstructor(any()))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.activateEducationInstructor(educationInstructorId, auditCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
    }

    @Nested
    class DeactivateEducationInstructorTests {

        @Test
        void shouldDeactivateEducationInstructorSuccessfully() {

            EducationInstructorResponse deactivatedResponse =
                    new EducationInstructorResponse(educationInstructorId, Instant.now(), false);

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.deactivateEducationInstructor(educationInstructorId))
                    .thenReturn(deactivatedResponse);
            when(auditlogPort.create(any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString()))
                    .thenReturn(auditlogId);
            when(auditlogPort.findById(auditlogId))
                    .thenReturn(auditlogId);

            EducationInstructorResponse result =
                    service.deactivateEducationInstructor(educationInstructorId, auditCmd);

            assertNotNull(result);
            assertEquals(educationInstructorId, result.id());
            assertFalse(result.isActive());

            verify(educationInstructorPort).findById(educationInstructorId);
            verify(educationInstructorPort).deactivateEducationInstructor(educationInstructorId);
            verify(sagaLock).release(sessionId);
            verify(sagaLock, never()).updateStatus(any(), eq(SagaStatus.COMPENSATING));
        }

        @Test
        void shouldThrowWhenEducationInstructorNotFoundOnDeactivate() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ConflictException.class,
                    () -> service.deactivateEducationInstructor(educationInstructorId, auditCmd));

            verify(educationInstructorPort, never()).deactivateEducationInstructor(any());
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenDeactivateResponseIsNull() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.deactivateEducationInstructor(educationInstructorId))
                    .thenReturn(null);

            assertThrows(
                    ConflictException.class,
                    () -> service.deactivateEducationInstructor(educationInstructorId, auditCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenDeactivateResponseIsStillActive() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.deactivateEducationInstructor(educationInstructorId))
                    .thenReturn(educationInstructorResponse);

            assertThrows(
                    ConflictException.class,
                    () -> service.deactivateEducationInstructor(educationInstructorId, auditCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWhenDeactivateAuditCreationFails() {

            EducationInstructorResponse deactivatedResponse =
                    new EducationInstructorResponse(educationInstructorId, Instant.now(), false);

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));
            when(educationInstructorPort.deactivateEducationInstructor(educationInstructorId))
                    .thenReturn(deactivatedResponse);
            when(auditlogPort.create(any(), any(), any(),
                    anyString(), anyString(), anyString(),
                    anyString(), anyString()))
                    .thenThrow(new RuntimeException());

            assertThrows(
                    ConflictException.class,
                    () -> service.deactivateEducationInstructor(educationInstructorId, auditCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateOnRuntimeExceptionDuringDeactivateEducationInstructor() {

            when(sagaLock.tryLock(any())).thenReturn(true);
            when(educationInstructorPort.findById(educationInstructorId))
                    .thenReturn(Optional.of(educationInstructorResponse));

            // simulate runtime exception inside deactivateEducationInstructor
            when(educationInstructorPort.deactivateEducationInstructor(any()))
                    .thenThrow(new RuntimeException("boom"));
            assertThrows(
                    RuntimeException.class,
                    () -> service.deactivateEducationInstructor(educationInstructorId, auditCmd)
            );
            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
            verify(sagaLock).release(sessionId);
        }
    }
}
