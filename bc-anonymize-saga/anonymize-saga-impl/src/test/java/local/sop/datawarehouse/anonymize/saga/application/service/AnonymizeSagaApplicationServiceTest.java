package local.sop.datawarehouse.anonymize.saga.application.service;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.exceptions.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.CreateAnonymizeCmd;
import local.sop.datawarehouse.anonymize.saga.application.ports.out.anonymize.AnonymizePort;
import local.sop.datawarehouse.anonymize.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.anonymize.saga.application.ports.out.person.PersonPort;
import local.sop.datawarehouse.anonymize.saga.application.service.AnonymizeSagaApplicationService;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;


import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
 class AnonymizeSagaApplicationServiceTest {
	
	@Mock
	private AuditlogPort auditlogPort;

	@Mock
	private AnonymizePort anonymizePort;

	@Mock
	private PersonPort personPort;

	private AnonymizeSagaApplicationService service;

	private UUID personId;
    private UUID anonymizeId;
	private CreateAnonymizeCmd cmd;
    private ResponseCompensated compensatedOk;

	@BeforeEach
	void setUp() {
		service = new AnonymizeSagaApplicationService(anonymizePort, personPort, auditlogPort);

		personId = UUID.randomUUID();
        anonymizeId = UUID.randomUUID();
		cmd = new CreateAnonymizeCmd(
			personId,
			UUID.randomUUID(),
			ActorType.USER,
			Severity.INFO,
			"originSystem",
			"originService",
			"originComponent",
			"data",
			"description"
		);

        compensatedOk = new ResponseCompensated(SagaOutcome.COMPENSATED, true);
	}

	/* =========================================================== *
     *  Step 1 -
     * =========================================================== */
	@Nested
	class Step1_AnonymizeCreate {
		@Test
		void create_ShouldThrowConflictException_whenAnonymizeCreateThrowsRuntimeException() {
			when(anonymizePort.create(cmd.personRef()))
				.thenThrow(new RuntimeException("downstream unavailable"));

				assertThrows(ConflictException.class, () -> service.create(cmd));
		}

		@Test
		void create_ShouldNotProceedToGetOrAuditlog_whenAnonymizeCreateFails(){
			when(anonymizePort.create(cmd.personRef()))
				.thenThrow(new RuntimeException("downstream unavailable"));

				assertThrows(ConflictException.class, () -> service.create(cmd));

				verify(personPort, never()).get(any());
				verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
		}

        @Test
        void create_ShouldNotCompensate_whenAnonymizeCreateFails() {
            when(anonymizePort.create(cmd.personRef()))
                .thenThrow(new RuntimeException("downstream unavailable"));

            assertThrows(ConflictException.class, () -> service.create(cmd));

            verify(anonymizePort, never()).compensate(any(), any(), any());
            verify(auditlogPort, never()).compensate(any(), any(), any());
        }
	}

    /* =========================================================== *
     *  Step 2 -
     * =========================================================== */
    @Nested
    class Step2_PersonGet {
        @BeforeEach
        void step1Succeeds() {
            when(anonymizePort.create(cmd.personRef())).thenReturn(anonymizeId);
        }

        @Test
        void create_ShouldThrowConflictException_whenPersonGetReturnsNull() {
            when(personPort.get(cmd.personRef())).thenReturn(null);
            when(anonymizePort.compensate(eq(anonymizeId), any(), eq(SagaOutcome.COMPENSATED)))
                .thenReturn(compensatedOk);

            assertThrows(ConflictException.class, () -> service.create(cmd));
        }

        @Test
        void create_ShouldCompensateAnonymize_whenPersonGetReturnsNull() {
            when(personPort.get(cmd.personRef())).thenReturn(null);
            when(anonymizePort.compensate(eq(anonymizeId), any(), eq(SagaOutcome.COMPENSATED)))
                .thenReturn(compensatedOk);

            assertThrows(ConflictException.class, () -> service.create(cmd));

            verify(anonymizePort).compensate(anonymizeId, service.getClass(), SagaOutcome.COMPENSATED);
        }

        @Test
        void create_ShouldNotCallAuditlog_whenPersonGetReturnsNull() {
            when(personPort.get(cmd.personRef())).thenReturn(null);
            when(anonymizePort.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class, () -> service.create(cmd));

            verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void create_ShouldCompensateAndThrowConflictException_whenPersonGetThrowsRuntimeException() {
            when(personPort.get(cmd.personRef())).thenThrow(new RuntimeException("person service unavailable"));
            when(anonymizePort.compensate(eq(anonymizeId), any(), eq(SagaOutcome.COMPENSATED)))
                .thenReturn(compensatedOk);

            assertThrows(ConflictException.class, () -> service.create(cmd));

            verify(anonymizePort).compensate(anonymizeId, service.getClass(), SagaOutcome.COMPENSATED);
            verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void create_ShouldNotCompensate_whenPersonGetThrowsDomainException() {
            DomainException domainException = new DomainException(ErrorCode.CONFLICT, "person.domain.error");
            when(personPort.get(cmd.personRef())).thenThrow(domainException);

            DomainException thrown = assertThrows(DomainException.class, () -> service.create(cmd));

            assertSame(domainException, thrown);
            verify(anonymizePort, never()).compensate(any(), any(), any());
            verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void create_ShouldProceedToAuditlog_whenPersonGetReturnsPerson() {
            PersonResponse person = new PersonResponse(
                cmd.personRef(),
                "Jane",
                "Doe",
                "jane.doe@example.com",
                UUID.randomUUID(),
                List.of()
            );
            when(personPort.get(cmd.personRef())).thenReturn(person);
            when(auditlogPort.create(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(UUID.randomUUID());

            service.create(cmd);

            verify(anonymizePort, never()).compensate(any(), any(), any());
            verify(auditlogPort).create(any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    /* =========================================================== *
     *  Step 3 -
     * =========================================================== */
    @Nested
    class Step3_AuditlogCreate {
        @BeforeEach
        void step1And2Succeed() {
            when(anonymizePort.create(cmd.personRef())).thenReturn(anonymizeId);
            PersonResponse person = new PersonResponse(
                cmd.personRef(),
                "Jane",
                "Doe",
                "jane.doe@example.com",
                UUID.randomUUID(),
                List.of()
            );
            when(personPort.get(cmd.personRef())).thenReturn(person);
        }

        @Test
        void create_ShouldReturnSuccessAndNotCompensate_whenAuditlogCreateSucceeds() {
            when(auditlogPort.create(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(UUID.randomUUID());

            service.create(cmd);

            verify(auditlogPort).create(any(), any(), any(), any(), any(), any(), any(), any());
            verify(anonymizePort, never()).compensate(any(), any(), any());
            verify(auditlogPort, never()).compensate(any(), any(), any());
        }

        @Test
        void create_ShouldRethrowDomainException_whenAuditlogCreateThrowsDomainException() {
            DomainException domainException = new DomainException(ErrorCode.CONFLICT, "auditlog.domain.error");
            when(auditlogPort.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(domainException);

            DomainException thrown = assertThrows(DomainException.class, () -> service.create(cmd));

            assertSame(domainException, thrown);
            verify(anonymizePort, never()).compensate(any(), any(), any());
            verify(auditlogPort, never()).compensate(any(), any(), any());
        }

        @Test
        void create_ShouldCompensateAnonymizeAndThrowConflict_whenAuditlogCreateThrowsRuntimeException() {
            when(auditlogPort.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("auditlog unavailable"));
            when(anonymizePort.compensate(eq(anonymizeId), any(), eq(SagaOutcome.COMPENSATED)))
                .thenReturn(compensatedOk);

            assertThrows(ConflictException.class, () -> service.create(cmd));

            verify(anonymizePort).compensate(anonymizeId, service.getClass(), SagaOutcome.COMPENSATED);
            verify(auditlogPort, never()).compensate(any(), any(), any());
        }
    }
}
