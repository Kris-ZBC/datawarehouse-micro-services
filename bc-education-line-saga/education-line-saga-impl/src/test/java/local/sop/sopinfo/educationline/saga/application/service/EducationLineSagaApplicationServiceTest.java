package local.sop.sopinfo.educationline.saga.application.service;

import java.lang.reflect.Field;
import java.util.UUID;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import local.sop.sopinfo.educationline.saga.application.api.dto.*;
import local.sop.sopinfo.educationline.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.educationline.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.educationline.saga.application.ports.out.education.EducationPort;
import local.sop.sopinfo.educationline.saga.application.ports.out.educationline.EducationLinePort;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.exceptions.ErrorCode;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

@ExtendWith(MockitoExtension.class)
class EducationLineSagaApplicationServiceTest {

	@Mock
	private EducationLinePort educationLinePort;

	@Mock
	private EducationPort educationPort;

	@Mock
	private AuditlogPort auditlogPort;

	private EducationLineSagaApplicationService service;

	// Default properties for test
	private UUID educationLineId;
	private UUID educationRefId;
	private UUID auditlogId;
	private CreateEducationLineCmd cmd;
	private UpdateEducationLineNameCmd nameCmd;
	private UpdateEducationLineDurationCmd durationCmd;
	private CreateAuditlogCmd auditlogCmd;
	private EducationLineResponse response;
	private EducationLineResponse nameResponse;
	private EducationLineResponse durationResponse;
	private EducationLineResponse deactivatedResponse;
	private EducationResponse educationResponse;
	private ResponseCompensated compensatedSuccess;

	@BeforeEach
	void setUp() {
		this.service = new EducationLineSagaApplicationService(educationLinePort, educationPort, auditlogPort);

		educationLineId = UUID.randomUUID();
		educationRefId = UUID.randomUUID();
		auditlogId = UUID.randomUUID();

		educationResponse = new EducationResponse(educationRefId, "EDUCATION", "CATEGORY", true);

		cmd = new CreateEducationLineCmd(
				"TEST",
				1,
				2,
				3,
				educationRefId,
				UUID.randomUUID(),
				ActorType.USER,
				Severity.INFO,
				"originSystem",
				"originService",
				"originComponent",
				"data",
				"description");

		nameCmd = new UpdateEducationLineNameCmd(
				"UPDATED_TEST",
				UUID.randomUUID(),
				ActorType.USER,
				Severity.INFO,
				"originSystem",
				"originService",
				"originComponent",
				"data",
				"description");

		durationCmd = new UpdateEducationLineDurationCmd(
				2,
				3,
				4,
				UUID.randomUUID(),
				ActorType.USER,
				Severity.INFO,
				"originSystem",
				"originService",
				"originComponent",
				"data",
				"description");

		auditlogCmd = new CreateAuditlogCmd(
				UUID.randomUUID(),
				ActorType.USER,
				Severity.INFO,
				"originSystem",
				"originService",
				"originComponent",
				"data",
				"description");

		response = new EducationLineResponse(
				educationLineId,
				cmd.name(),
				cmd.durationYears(),
				cmd.durationMonths(),
				cmd.durationDays(),
				cmd.educationRef(),
				Instant.now(),
				true);
		nameResponse = new EducationLineResponse(
				educationLineId,
				nameCmd.name(),
				cmd.durationYears(),
				cmd.durationMonths(),
				cmd.durationDays(),
				cmd.educationRef(),
				Instant.now(),
				true);
		durationResponse = new EducationLineResponse(
				educationLineId,
				cmd.name(),
				durationCmd.durationYears(),
				durationCmd.durationMonths(),
				durationCmd.durationDays(),
				cmd.educationRef(),
				Instant.now(),
				true);
		deactivatedResponse = new EducationLineResponse(
				educationLineId,
				cmd.name(),
				cmd.durationYears(),
				cmd.durationMonths(),
				cmd.durationDays(),
				cmd.educationRef(),
				Instant.now(),
				false);
		compensatedSuccess = new ResponseCompensated(SagaOutcome.COMPENSATED, true);
	}

	@Nested
	class HappyPathTests {

		@Test
		void create_shouldReturnEducationLineResponse_whenCmdIsValid() {
			when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
			when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
					cmd.durationDays(), cmd.educationRef()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
			when(auditlogPort.create(
					cmd.actorRef(),
					cmd.actorType(),
					cmd.severity(),
					cmd.originSystem(),
					cmd.originService(),
					cmd.originComponent(),
					cmd.data(),
					cmd.description()))
					.thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			EducationLineResponse result = service.createEducationLine(cmd);

			assertNotNull(result);
			assertEquals(educationLineId, result.id());
			assertEquals(cmd.name(), result.name());
			assertEquals(cmd.durationYears(), result.durationYears());
			assertEquals(cmd.durationMonths(), result.durationMonths());
			assertEquals(cmd.durationDays(), result.durationDays());
			assertEquals(cmd.educationRef(), result.educationRef());
			assertTrue(result.isActive());
		}

		@Test
		void create_shouldCreateInOrder_whenCmdIsValid() {
			when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
			when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
					cmd.durationDays(), cmd.educationRef()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
			when(auditlogPort.create(
					cmd.actorRef(),
					cmd.actorType(),
					cmd.severity(),
					cmd.originSystem(),
					cmd.originService(),
					cmd.originComponent(),
					cmd.data(),
					cmd.description()))
					.thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.createEducationLine(cmd);

			InOrder inOrder = inOrder(educationPort, educationLinePort, auditlogPort);
			inOrder.verify(educationPort).existsById(cmd.educationRef());
			inOrder.verify(educationLinePort).createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
					cmd.durationDays(), cmd.educationRef());
			inOrder.verify(educationLinePort).findEducationLineById(educationLineId);
			inOrder.verify(auditlogPort).create(
					cmd.actorRef(),
					cmd.actorType(),
					cmd.severity(),
					cmd.originSystem(),
					cmd.originService(),
					cmd.originComponent(),
					cmd.data(),
					cmd.description());
		}

		@Test
		void create_shouldNeverCallCompensate_whenCmdIsValid() {
			when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
			when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
					cmd.durationDays(), cmd.educationRef()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
			when(auditlogPort.create(
					cmd.actorRef(),
					cmd.actorType(),
					cmd.severity(),
					cmd.originSystem(),
					cmd.originService(),
					cmd.originComponent(),
					cmd.data(),
					cmd.description()))
					.thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.createEducationLine(cmd);

			verify(educationLinePort, never()).compensate(any(), any(), any());
			verify(auditlogPort, never()).compensate(any(), any(), any());
		}

		@Test
		void updateName_shouldReturnEducationLineResponse_whenCmdIsValid() {
			when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(nameResponse);
			when(auditlogPort.create(
					nameCmd.actorRef(),
					nameCmd.actorType(),
					nameCmd.severity(),
					nameCmd.originSystem(),
					nameCmd.originService(),
					nameCmd.originComponent(),
					nameCmd.data(),
					nameCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			EducationLineResponse result = service.updateEducationLineName(educationLineId, nameCmd);

			assertNotNull(result);
			assertEquals(educationLineId, result.id());
			assertEquals(nameCmd.name(), result.name());
		}

		@Test
		void updateName_shouldUpdateInOrder_whenCmdIsValid() {
			when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(nameResponse);
			when(auditlogPort.create(
					nameCmd.actorRef(),
					nameCmd.actorType(),
					nameCmd.severity(),
					nameCmd.originSystem(),
					nameCmd.originService(),
					nameCmd.originComponent(),
					nameCmd.data(),
					nameCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.updateEducationLineName(educationLineId, nameCmd);

			InOrder inOrder = inOrder(educationLinePort, auditlogPort);
			inOrder.verify(educationLinePort).updateEducationLineName(educationLineId, nameCmd.name());
			inOrder.verify(auditlogPort).create(
					nameCmd.actorRef(),
					nameCmd.actorType(),
					nameCmd.severity(),
					nameCmd.originSystem(),
					nameCmd.originService(),
					nameCmd.originComponent(),
					nameCmd.data(),
					nameCmd.description());
		}

		@Test
		void updateName_shouldNeverCallCompensate_whenCmdIsValid() {
			when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(nameResponse);
			when(auditlogPort.create(
					nameCmd.actorRef(),
					nameCmd.actorType(),
					nameCmd.severity(),
					nameCmd.originSystem(),
					nameCmd.originService(),
					nameCmd.originComponent(),
					nameCmd.data(),
					nameCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.updateEducationLineName(educationLineId, nameCmd);

			verify(educationLinePort, never()).compensate(any(), any(), any());
			verify(educationLinePort, never()).compensateUpdateName(any(), any(), any(), any());
			verify(auditlogPort, never()).compensate(any(), any(), any());
		}

		@Test
		void updateDuration_shouldReturnEducationLineResponse_whenCmdIsValid() {
			when(educationLinePort.updateEducationLineDuration(
					educationLineId,
					durationCmd.durationYears(),
					durationCmd.durationMonths(),
					durationCmd.durationDays()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(durationResponse);
			when(auditlogPort.create(
					durationCmd.actorRef(),
					durationCmd.actorType(),
					durationCmd.severity(),
					durationCmd.originSystem(),
					durationCmd.originService(),
					durationCmd.originComponent(),
					durationCmd.data(),
					durationCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			EducationLineResponse result = service.updateEducationLineDuration(educationLineId, durationCmd);

			assertNotNull(result);
			assertEquals(educationLineId, result.id());
			assertEquals(durationCmd.durationYears(), result.durationYears());
			assertEquals(durationCmd.durationMonths(), result.durationMonths());
			assertEquals(durationCmd.durationDays(), result.durationDays());
		}

		@Test
		void updateDuration_shouldUpdateInOrder_whenCmdIsValid() {
			when(educationLinePort.updateEducationLineDuration(
					educationLineId,
					durationCmd.durationYears(),
					durationCmd.durationMonths(),
					durationCmd.durationDays()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(durationResponse);
			when(auditlogPort.create(
					durationCmd.actorRef(),
					durationCmd.actorType(),
					durationCmd.severity(),
					durationCmd.originSystem(),
					durationCmd.originService(),
					durationCmd.originComponent(),
					durationCmd.data(),
					durationCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.updateEducationLineDuration(educationLineId, durationCmd);

			InOrder inOrder = inOrder(educationLinePort, auditlogPort);
			inOrder.verify(educationLinePort).updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
					durationCmd.durationMonths(), durationCmd.durationDays());
			inOrder.verify(auditlogPort).create(
					durationCmd.actorRef(),
					durationCmd.actorType(),
					durationCmd.severity(),
					durationCmd.originSystem(),
					durationCmd.originService(),
					durationCmd.originComponent(),
					durationCmd.data(),
					durationCmd.description());
		}

		@Test
		void updateDuration_shouldNeverCallCompensate_whenCmdIsValid() {
			when(educationLinePort.updateEducationLineDuration(
					educationLineId,
					durationCmd.durationYears(),
					durationCmd.durationMonths(),
					durationCmd.durationDays()))
					.thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(durationResponse);
			when(auditlogPort.create(
					durationCmd.actorRef(),
					durationCmd.actorType(),
					durationCmd.severity(),
					durationCmd.originSystem(),
					durationCmd.originService(),
					durationCmd.originComponent(),
					durationCmd.data(),
					durationCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.updateEducationLineDuration(educationLineId, durationCmd);

			verify(educationLinePort, never()).compensate(any(), any(), any());
			verify(educationLinePort, never()).compensateUpdateDuration(any(), any(), any(), anyInt(), anyInt(),
					anyInt());
			verify(auditlogPort, never()).compensate(any(), any(), any());
		}

		@Test
		void activate_shouldReturnEducationLineResponse_whenCmdIsValid() {
			when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
			when(auditlogPort.create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			EducationLineResponse result = service.activateEducationLine(educationLineId, auditlogCmd);

			assertNotNull(result);
			assertEquals(educationLineId, result.id());
			assertTrue(result.isActive());/**
											 * ADD AUDITLOG HERE AS WELL, MIGHT NEED TO UPDATE SERVICE TO SUPPORT IT
											 * AND ADD A DTO JUST TO HOLD THE VARIABLES NEEDED FOR AUDITLOG
											 * TO BE USED BY DEACTIVATE AND ACTIVATE
											 */
		}

		@Test
		void activate_shouldActivateInOrder_whenCmdIsValid() {
			when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
			when(auditlogPort.create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.activateEducationLine(educationLineId, auditlogCmd);

			InOrder inOrder = inOrder(educationLinePort, auditlogPort);
			inOrder.verify(educationLinePort).activateEducationLine(educationLineId);
			inOrder.verify(auditlogPort).create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description());
		}

		@Test
		void activate_shouldNeverCallCompensate_whenCmdIsValid() {
			when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
			when(auditlogPort.create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.activateEducationLine(educationLineId, auditlogCmd);

			verify(educationLinePort, never()).compensate(any(), any(), any());
			verify(educationLinePort, never()).compensateActivate(any(), any(), any());
			verify(auditlogPort, never()).compensate(any(), any(), any());
		}

		@Test
		void deactivate_shouldReturnEducationLineResponse_whenCmdIsValid() {
			when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
			when(auditlogPort.create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			EducationLineResponse result = service.deactivateEducationLine(educationLineId, auditlogCmd);

			assertNotNull(result);
			assertEquals(educationLineId, result.id());
			assertFalse(result.isActive());
		}

		@Test
		void deactivate_shouldDeactivateInOrder_whenCmdIsValid() {
			when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
			when(auditlogPort.create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.deactivateEducationLine(educationLineId, auditlogCmd);

			InOrder inOrder = inOrder(educationLinePort, auditlogPort);
			inOrder.verify(educationLinePort).deactivateEducationLine(educationLineId);
			inOrder.verify(auditlogPort).create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description());
		}

		@Test
		void deactivate_shouldNeverCallCompensate_whenCmdIsValid() {
			when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
			when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
			when(auditlogPort.create(
					auditlogCmd.actorRef(),
					auditlogCmd.actorType(),
					auditlogCmd.severity(),
					auditlogCmd.originSystem(),
					auditlogCmd.originService(),
					auditlogCmd.originComponent(),
					auditlogCmd.data(),
					auditlogCmd.description())).thenReturn(auditlogId);
			when(auditlogPort.findById(auditlogId)).thenReturn(auditlogId);

			service.deactivateEducationLine(educationLineId, auditlogCmd);

			verify(educationLinePort, never()).compensate(any(), any(), any());
			verify(educationLinePort, never()).compensateDeactivate(any(), any(), any());
			verify(auditlogPort, never()).compensate(any(), any(), any());
		}
	}

	@Nested
	class SadPathTests {

		/* ========== First step - Create / Update Education Line fails ========== */
		@Nested
		class FirstStep_EducationLineCreationOrUpdateFails {

			@Test
			void create_shouldThrowConflictException_whenEducationRefDoesNotExist() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(null);

				assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

				verify(educationLinePort, never()).createEducationLine(any(), anyInt(), anyInt(), anyInt(), any());
				verify(educationLinePort, never()).compensate(any(), any(), any());
				verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
				verify(auditlogPort, never()).compensate(any(), any(), any());
			}

			@Test
			void create_shouldThrowException_whenEducationLineCreateFails() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenThrow(new ConflictException("educationline.create.failed",
								Map.of("object", "educationLine")));
				
				assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
				verify(educationLinePort).createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef());
			}

			@Test
			void create_shouldThrowConflictException_whenEducationLineAlreadyExists() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenThrow(new ConflictException("educationline.already.exists", Map.of("id", educationLineId.toString())));

				assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
			}

			@Test
			void create_shouldThrowDomainException_whenEducationLineCreationFails() {
				DomainException domainException = new DomainException(ErrorCode.CONFLICT,
						"educationline.already.exists",
						Map.of("id", educationLineId.toString()));
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenThrow(domainException);

				DomainException thrown = assertThrows(DomainException.class,
						() -> service.createEducationLine(cmd));
				assertSame(domainException, thrown);
			}

			@Test
			void create_shouldNotWrapDomainException_whenEducationLineCreationFails() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.already.exists",
								Map.of("id", educationLineId.toString())));

				Exception ex = assertThrows(DomainException.class, () -> service.createEducationLine(cmd));
				// Makes sure that the DomainException thrown by the port is not wrapped in
				// another exception by the service, but is re-thrown as is
				assertFalse(ex instanceof ConflictException);
			}

			@Test
			void create_shouldNeverSanityCheck_whenEducationLineCreationFails() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenThrow(new RuntimeException("educationline.already.exists"));

				assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

				verify(educationLinePort).createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef());
				verify(educationLinePort, never()).findEducationLineById(any());
			}

			@Test
			void create_shouldNeverCompensate_whenEducationLineCreationFails() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenThrow(new RuntimeException("educationline.already.exists"));

				assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

				verify(educationLinePort, never()).compensate(any(), any(), any());
				verify(auditlogPort, never()).compensate(any(), any(), any());
			}

			@Test
			void create_shouldCompensateEducationLine_whenAuditlogCreateFails() {
				when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
				when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
						cmd.durationDays(), cmd.educationRef()))
						.thenReturn(educationLineId);
				when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
				when(auditlogPort.create(
						cmd.actorRef(),
						cmd.actorType(),
						cmd.severity(),
						cmd.originSystem(),
						cmd.originService(),
						cmd.originComponent(),
						cmd.data(),
						cmd.description()))
						.thenThrow(new ConflictException("auditlog.create.failed", Map.of("object", "auditlog")));
				when(educationLinePort.compensate(any(), any(), any())).thenReturn(compensatedSuccess);

				assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

				verify(educationLinePort).compensate(any(), any(), any());
			}

			@Test
			void updateName_shouldThrowConflictException_whenEducationLineDoesNotExist() {
				when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineName(educationLineId, nameCmd));
			}

			@Test
			void updateName_shouldReThrowDomainException_whenEducationLineNameUpdateFails() {
				DomainException domainException = new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
						Map.of("id", educationLineId.toString()));
				when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
						.thenThrow(domainException);

				DomainException thrown = assertThrows(DomainException.class,
						() -> service.updateEducationLineName(educationLineId, nameCmd));
				assertSame(domainException, thrown);
			}

			@Test
			void updateName_shouldNotWrapDomainException_whenEducationLineNameUpdateFails() {
				when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
						.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
								Map.of("id", educationLineId.toString())));

				Exception ex = assertThrows(DomainException.class,
						() -> service.updateEducationLineName(educationLineId, nameCmd));
				// Makes sure that the DomainException thrown by the port is not wrapped in
				// another exception by the service, but is re-thrown as is
				assertFalse(ex instanceof ConflictException);
			}

			@Test
			void updateName_shouldNeverSanityCheck_whenEducationLineNameUpdateFails() {
				when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineName(educationLineId, nameCmd));

				verify(educationLinePort).updateEducationLineName(educationLineId, nameCmd.name());
				verify(educationLinePort).findEducationLineById(educationLineId);
				verify(educationLinePort, never()).compensateUpdateName(any(), any(), any(), any());
			}

			@Test
			void updateName_shouldNeverCompensate_whenEducationLineNameUpdateFails() {
				when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineName(educationLineId, nameCmd));

				verify(educationLinePort, never()).compensate(any(), any(), any());
				verify(educationLinePort, never()).compensateUpdateName(any(), any(), any(), any());
				verify(auditlogPort, never()).compensate(any(), any(), any());
			}

			@Test
			void updateName_shouldThrowConflictException_whenEducationLineVerificationReturnsDifferentName() {
				when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
						.thenReturn(educationLineId);
				when(educationLinePort.findEducationLineById(educationLineId))
						.thenReturn(new EducationLineResponse(
								educationLineId,
								"WRONG",
								cmd.durationYears(),
								cmd.durationMonths(),
								cmd.durationDays(),
								cmd.educationRef(),
								Instant.now(),
								true));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineName(educationLineId, nameCmd));
			}

			@Test
			void updateDuration_shouldThrowConflictException_whenEducationLineDoesNotExist() {
				when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays()))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineDuration(educationLineId, durationCmd));
			}

			@Test
			void updateDuration_shouldReThrowDomainException_whenEducationLineDurationUpdateFails() {
				DomainException domainException = new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
						Map.of("id", educationLineId.toString()));
				when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays()))
						.thenThrow(domainException);

				DomainException thrown = assertThrows(DomainException.class,
						() -> service.updateEducationLineDuration(educationLineId, durationCmd));
				assertSame(domainException, thrown);
			}

			@Test
			void updateDuration_shouldNotWrapDomainException_whenEducationLineDurationUpdateFails() {
				when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays()))
						.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
								Map.of("id", educationLineId.toString())));

				Exception ex = assertThrows(DomainException.class,
						() -> service.updateEducationLineDuration(educationLineId, durationCmd));
				// Makes sure that the DomainException thrown by the port is not wrapped in
				// another exception by the service, but is re-thrown as is
				assertFalse(ex instanceof ConflictException);
			}

			@Test
			void updateDuration_shouldNeverSanityCheck_whenEducationLineDurationUpdateFails() {
				when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays()))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineDuration(educationLineId, durationCmd));

				verify(educationLinePort).updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays());
				verify(educationLinePort).findEducationLineById(educationLineId);
				verify(educationLinePort, never()).compensateUpdateDuration(any(), any(), any(), anyInt(), anyInt(),
						anyInt());
			}

			@Test
			void updateDuration_shouldNeverCompensate_whenEducationLineDurationUpdateFails() {
				when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays()))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineDuration(educationLineId, durationCmd));

				verify(educationLinePort, never()).compensate(any(), any(), any());
				verify(educationLinePort, never()).compensateUpdateDuration(any(), any(), any(), anyInt(), anyInt(),
						anyInt());
				verify(auditlogPort, never()).compensate(any(), any(), any());
			}

			@Test
			void updateDuration_shouldThrowConflictException_whenEducationLineVerificationReturnsDifferentDuration() {
				when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
						durationCmd.durationMonths(), durationCmd.durationDays()))
						.thenReturn(educationLineId);
				when(educationLinePort.findEducationLineById(educationLineId))
						.thenReturn(new EducationLineResponse(
								educationLineId,
								cmd.name(),
								99,
								98,
								97,
								cmd.educationRef(),
								Instant.now(),
								true));

				assertThrows(ConflictException.class,
						() -> service.updateEducationLineDuration(educationLineId, durationCmd));
			}

			@Test
			void activate_shouldThrowConflictException_whenEducationLineDoesNotExist() {
				when(educationLinePort.activateEducationLine(educationLineId))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.activateEducationLine(educationLineId, auditlogCmd));
			}

			@Test
			void activate_shouldReThrowDomainException_whenEducationLineActivationFails() {
				DomainException domainException = new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
						Map.of("id", educationLineId.toString()));
				when(educationLinePort.activateEducationLine(educationLineId))
						.thenThrow(domainException);

				DomainException thrown = assertThrows(DomainException.class,
						() -> service.activateEducationLine(educationLineId, auditlogCmd));
				assertSame(domainException, thrown);
			}

			@Test
			void activate_shouldNotWrapDomainException_whenEducationLineActivationFails() {
				when(educationLinePort.activateEducationLine(educationLineId))
						.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
								Map.of("id", educationLineId.toString())));

				Exception ex = assertThrows(DomainException.class,
						() -> service.activateEducationLine(educationLineId, auditlogCmd));
				// Makes sure that the DomainException thrown by the port is not wrapped in
				// another exception by the service, but is re-thrown as is
				assertFalse(ex instanceof ConflictException);
			}

			@Test
			void activate_shouldNeverSanityCheck_whenEducationLineActivationFails() {
				when(educationLinePort.activateEducationLine(educationLineId))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.activateEducationLine(educationLineId, auditlogCmd));

				verify(educationLinePort).activateEducationLine(educationLineId);
				verify(educationLinePort, never()).findEducationLineById(any());
			}

			@Test
			void activate_shouldNeverCompensate_whenEducationLineActivationFails() {
				when(educationLinePort.activateEducationLine(educationLineId))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.activateEducationLine(educationLineId, auditlogCmd));

				verify(educationLinePort, never()).compensate(any(), any(), any());
				verify(educationLinePort, never()).compensateActivate(any(), any(), any());
				verify(auditlogPort, never()).compensate(any(), any(), any());
			}

			@Test
			void activate_shouldThrowConflictException_whenEducationLineVerificationReturnsInactive() {
				when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
				when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);

				assertThrows(ConflictException.class,
						() -> service.activateEducationLine(educationLineId, auditlogCmd));
			}

			@Test
			void deactivate_shouldThrowConflictException_whenEducationLineDoesNotExist() {
				when(educationLinePort.deactivateEducationLine(educationLineId))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
			}

			@Test
			void deactivate_shouldReThrowDomainException_whenEducationLineDeactivationFails() {
				DomainException domainException = new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
						Map.of("id", educationLineId.toString()));
				when(educationLinePort.deactivateEducationLine(educationLineId))
						.thenThrow(domainException);

				DomainException thrown = assertThrows(DomainException.class,
						() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				assertSame(domainException, thrown);
			}

			@Test
			void deactivate_shouldNotWrapDomainException_whenEducationLineDeactivationFails() {
				when(educationLinePort.deactivateEducationLine(educationLineId))
						.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
								Map.of("id", educationLineId.toString())));

				Exception ex = assertThrows(DomainException.class,
						() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				// Makes sure that the DomainException thrown by the port is not wrapped in
				// another exception by the service, but is re-thrown as is
				assertFalse(ex instanceof ConflictException);
			}

			@Test
			void deactivate_shouldNeverSanityCheck_whenEducationLineDeactivationFails() {
				when(educationLinePort.deactivateEducationLine(educationLineId))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.deactivateEducationLine(educationLineId, auditlogCmd));

				verify(educationLinePort).deactivateEducationLine(educationLineId);
				verify(educationLinePort, never()).findEducationLineById(any());
			}

			@Test
			void deactivate_shouldNeverCompensate_whenEducationLineDeactivationFails() {
				when(educationLinePort.deactivateEducationLine(educationLineId))
						.thenThrow(new RuntimeException("educationline.not.found"));

				assertThrows(ConflictException.class,
						() -> service.deactivateEducationLine(educationLineId, auditlogCmd));

				verify(educationLinePort, never()).compensate(any(), any(), any());
				verify(educationLinePort, never()).compensateDeactivate(any(), any(), any());
				verify(auditlogPort, never()).compensate(any(), any(), any());
			}

			@Test
			void deactivate_shouldThrowConflictException_whenEducationLineVerificationReturnsActive() {
				when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
				when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);

				assertThrows(ConflictException.class,
						() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
			}
		}

		@Nested
		class FirstStep_InitialRefSanityCheckFails {

			@Nested
			class CreateEducationLine {
				@Test
				void create_shouldThrowConflictException_whenEducationReferenceLookupThrowsRuntimeException() {
					when(educationPort.existsById(cmd.educationRef()))
							.thenThrow(new RuntimeException("education.reference.lookup.failed"));

					assertThrows(RuntimeException.class, () -> service.createEducationLine(cmd));
				}

				@Test
				void create_shouldThrowDomainException_whenEducationReferenceLookupThrowsDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"education.reference.lookup.failed");
					when(educationPort.existsById(cmd.educationRef())).thenThrow(domainException);

					ConflictException thrown = assertThrows(ConflictException.class,
							() -> service.createEducationLine(cmd));
					assertNotNull(thrown);
				}
			}

			@Nested
			class UpdateEducationLineName {
				@Test
				void updateName_shouldThrowConflictException_whenUpdateThrowsRuntimeException() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenThrow(new RuntimeException("educationline.update.failed"));

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
				}

				@Test
				void updateName_shouldThrowDomainException_whenUpdateThrowsDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"educationline.update.failed");
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenThrow(domainException);

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
					assertSame(domainException, thrown);
				}
			}

			@Nested
			class UpdateEducationLineDuration {
				@Test
				void updateDuration_shouldThrowConflictException_whenUpdateThrowsRuntimeException() {
					when(educationLinePort.updateEducationLineDuration(educationLineId,
							durationCmd.durationYears(), durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenThrow(new RuntimeException("educationline.update.failed"));

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
				}

				@Test
				void updateDuration_shouldThrowDomainException_whenUpdateThrowsDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"educationline.update.failed");
					when(educationLinePort.updateEducationLineDuration(educationLineId,
							durationCmd.durationYears(), durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenThrow(domainException);

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
					assertSame(domainException, thrown);
				}
			}

			@Nested
			class ActivateEducationLine {
				@Test
				void activate_shouldThrowConflictException_whenActivateThrowsRuntimeException() {
					when(educationLinePort.activateEducationLine(educationLineId))
							.thenThrow(new RuntimeException("educationline.activate.failed"));

					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
				}

				@Test
				void activate_shouldThrowDomainException_whenActivateThrowsDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"educationline.activate.failed");
					when(educationLinePort.activateEducationLine(educationLineId)).thenThrow(domainException);

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
					assertSame(domainException, thrown);
				}
			}

			@Nested
			class DeactivateEducationLine {
				@Test
				void deactivate_shouldThrowConflictException_whenDeactivateThrowsRuntimeException() {
					when(educationLinePort.deactivateEducationLine(educationLineId))
							.thenThrow(new RuntimeException("educationline.deactivate.failed"));

					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				}

				@Test
				void deactivate_shouldThrowDomainException_whenDeactivateThrowsDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"educationline.deactivate.failed");
					when(educationLinePort.deactivateEducationLine(educationLineId)).thenThrow(domainException);

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
					assertSame(domainException, thrown);
				}
			}
		}

		/* ========== Second step - Create / Update Education Line sanity check fails ========== */
		@Nested
		class SecondStep_EducationLineSanityCheckFails {

			@Nested
			class CreateEducationLine {
				@BeforeEach
				void setUp() {
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
				}

				@Test
				void create_shouldThrowConflictException_whenEducationLineSanityCheckReturnsNull() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(null);

					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
				}

				@Test
				void create_shouldThrowConflictException_whenEducationLineSanityCheckFails() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
				}

				@Test
				void create_shouldCompensateCreateEducationLine_whenEducationLineSanityCheckFails() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);

					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

					verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
				}

				@Test
				void create_shouldHandleNullCompensationResult_whenEducationLineSanityCheckFailsAndCompensateReturnsNull() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(null);

					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

					verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
				}

				@Test
				void create_shouldNeverCallAuditlog_whenEducationLineSanityCheckFails() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));

					verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
				}

				@Test
				void create_shouldThrowDomainException_whenEducationLineSanityCheck_returnsDomainException() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.createEducationLine(cmd));
					assertTrue(thrown instanceof DomainException);
				}

				@Test
				void create_shouldThrowDomainException_whenEducationLineSanityCheckCompensationReturnsDomainException() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(), cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenThrow(new DomainException(ErrorCode.CONFLICT, "compensation.failed",
									Map.of("id", educationLineId.toString())));

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.createEducationLine(cmd));
					assertTrue(thrown instanceof DomainException);
				}
			}

			@Nested
			class UpdateEducationLineName {
				@BeforeEach
				void setUp() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
				}

				@Test
				void updateName_shouldThrowConflictException_whenEducationLineSanityCheckReturnsNull() {
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
				}

				@Test
				void updateName_shouldConflictException_whenEducationLineSanityCheckFails() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
				}

				@Test
				void updateName_shouldCompensateUpdateEducationLineName_whenEducationLineSanityCheckFails() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							"PREVIOUS_NAME",
							cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(),
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME")))
							.thenReturn(compensatedSuccess);

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));

					verify(educationLinePort).compensateUpdateName(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME"));
				}

				@Test
				void updateName_shouldHandleNullCompensationResult_whenEducationLineSanityCheckFailsAndCompensateReturnsNull() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							"PREVIOUS_NAME",
							cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(),
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME")))
							.thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));

					verify(educationLinePort).compensateUpdateName(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME"));
				}

				@Test
				void updateName_shouldNeverCallAuditlog_whenEducationLineSanityCheckFails() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));

					verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
				}

				@Test
				void updateName_shouldThrowDomainException_whenEducationLineSanityCheck_returnsDomainException() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
					assertTrue(thrown instanceof DomainException);
				}
			}

			@Nested
			class UpdateEducationLineDuration {
				@BeforeEach
				void setUp() {
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
				}

				@Test
				void updateDuration_shouldThrowConflictException_whenEducationLineSanityCheckReturnsNull() {
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
				}

				@Test
				void updateDuration_shouldConflictException_whenEducationLineSanityCheckFails() {
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
				}

				@Test
				void updateDuration_shouldCompensateUpdateEducationLineDuration_whenEducationLineSanityCheckFails() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							cmd.name(),
							1,
							2,
							3,
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateUpdateDuration(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3)))
							.thenReturn(compensatedSuccess);

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));

					verify(educationLinePort).compensateUpdateDuration(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3));
				}

				@Test
				void updateDuration_shouldHandleNullCompensationResult_whenEducationLineSanityCheckFailsAndCompensateReturnsNull() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							cmd.name(),
							1,
							2,
							3,
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateUpdateDuration(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3)))
							.thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));

					verify(educationLinePort).compensateUpdateDuration(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3));
				}

				@Test
				void updateDuration_shouldNeverCallAuditlog_whenEducationLineSanityCheckFails() {
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));

					verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
				}

				@Test
				void updateDuration_shouldThrowDomainException_whenEducationLineSanityCheck_returnsDomainException() {
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
					assertTrue(thrown instanceof DomainException);
				}
			}

			@Nested
			class ActivateEducationLine {

				@BeforeEach
				void setUp() {
					when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
				}

				@Test
				void activate_shouldThrowConflictException_whenEducationLineSanityCheckReturnsNull() {
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
				}

				@Test
				void activate_shouldConflictException_whenEducationLineSanityCheckFails() {
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
				}

				@Test
				void activate_shouldCompensateActivateEducationLine_whenEducationLineSanityCheckFails() {
					when(educationLinePort.activateEducationLine(educationLineId))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateActivate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);

					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));

					verify(educationLinePort).compensateActivate(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED));
				}

				@Test
				void activate_shouldHandleNullCompensationResult_whenEducationLineSanityCheckFailsAndCompensateReturnsNull() {
					when(educationLinePort.activateEducationLine(educationLineId))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateActivate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));

					verify(educationLinePort).compensateActivate(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED));
				}

				@Test
				void activate_shouldNeverCallAuditlog_whenEducationLineSanityCheckFails() {
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));

					verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
				}

				@Test
				void activate_shouldThrowDomainException_whenEducationLineSanityCheck_returnsDomainException() {
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
					assertTrue(thrown instanceof DomainException);
				}
			}

			@Nested
			class DeactivateEducationLine {

				@BeforeEach
				void setUp() {
					when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
				}

				@Test
				void deactivate_shouldThrowConflictException_whenEducationLineSanityCheckReturnsNull() {
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				}

				@Test
				void deactivate_shouldConflictException_whenEducationLineSanityCheckFails() {
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				}

				@Test
				void deactivate_shouldCompensateDeactivateEducationLine_whenEducationLineSanityCheckFails() {
					when(educationLinePort.deactivateEducationLine(educationLineId))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateDeactivate(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);

					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));

					verify(educationLinePort).compensateDeactivate(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED));
				}

				@Test
				void deactivate_shouldHandleNullCompensationResult_whenEducationLineSanityCheckFailsAndCompensateReturnsNull() {
					when(educationLinePort.deactivateEducationLine(educationLineId))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new RuntimeException("educationline.not.found"));
					when(educationLinePort.compensateDeactivate(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED)))
							.thenReturn(null);

					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));

					verify(educationLinePort).compensateDeactivate(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED));
				}

				@Test
				void deactivate_shouldNeverCallAuditlog_whenEducationLineSanityCheckFails() {
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new ConflictException("educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));

					verify(auditlogPort, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
				}

				@Test
				void deactivate_shouldThrowDomainException_whenEducationLineSanityCheck_returnsDomainException() {
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenThrow(new DomainException(ErrorCode.CONFLICT, "educationline.not.found",
									Map.of("educationLineId", educationLineId)));

					DomainException thrown = assertThrows(DomainException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
					assertTrue(thrown instanceof DomainException);
				}
			}
		}

		/* ========== Third step - Auditlog creation fails ========== */
		@Nested
		class ThirdStep_AuditlogCreationFails {
	
			@Nested
			class CreateEducationLine {
				@Test
				void create_shouldThrowRuntimeException_whenAuditlogCreationFails() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							cmd.actorRef(),
							cmd.actorType(),
							cmd.severity(),
							cmd.originSystem(),
							cmd.originService(),
							cmd.originComponent(),
							cmd.data(),
							cmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);
					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
				}
	
				@Test
				void create_shouldCompensateEducationLineCreation_whenAuditlogCreationFails() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							cmd.actorRef(),
							cmd.actorType(),
							cmd.severity(),
							cmd.originSystem(),
							cmd.originService(),
							cmd.originComponent(),
							cmd.data(),
							cmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
	
					verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
					verify(auditlogPort, never()).compensate(any(), any(), any());
				}
	
				@Test
				void create_shouldThrowDomainException_whenAuditlogCreationFailsWithDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"auditlog.creation.failed");
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							cmd.actorRef(),
							cmd.actorType(),
							cmd.severity(),
							cmd.originSystem(),
							cmd.originService(),
							cmd.originComponent(),
							cmd.data(),
							cmd.description())).thenThrow(domainException);
	
					DomainException thrown = assertThrows(DomainException.class,
							() -> service.createEducationLine(cmd));
					assertSame(domainException, thrown);
				}
	
				@Test
				void create_shouldHandleNullCompensationResult_whenEducationLineCompensateReturnsNull() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							cmd.actorRef(),
							cmd.actorType(),
							cmd.severity(),
							cmd.originSystem(),
							cmd.originService(),
							cmd.originComponent(),
							cmd.data(),
							cmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(null);
	
					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
	
					verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
				}
	
				@Test
				void create_shouldHandleNullAuditlogCompensationResult_whenAuditlogCompensateReturnsNull() {
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							cmd.actorRef(),
							cmd.actorType(),
							cmd.severity(),
							cmd.originSystem(),
							cmd.originService(),
							cmd.originComponent(),
							cmd.data(),
							cmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
	
					// When auditlogPort.create() throws, auditlogId stays null, so no auditlog
					// compensation is called
					verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
					verify(auditlogPort, never()).compensate(any(), any(), any());
				}
	
				@Test
				void create_shouldCompensateAuditlog_whenAuditlogIdReturnsAndThrowsDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"compensation.failed", Map.of("id", educationLineId.toString()));
					when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
					when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(), cmd.educationRef()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							cmd.actorRef(),
							cmd.actorType(),
							cmd.severity(),
							cmd.originSystem(),
							cmd.originService(),
							cmd.originComponent(),
							cmd.data(),
							cmd.description())).thenReturn(auditlogId)
							.thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(auditlogPort.findById(auditlogId)).thenReturn(null);
					when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenThrow(domainException);
	
					ConflictException thrown = assertThrows(ConflictException.class,
							() -> service.createEducationLine(cmd));
					assertEquals("auditlog.not.found", thrown.getMessage());
	
					verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
					verify(auditlogPort).compensate(any(), any(), any());
				}
			}
	
			@Nested
			class UpdateEducationLineName {
				@Test
				void updateName_shouldThrowRuntimeException_whenAuditlogCreationFails() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(nameResponse);
					when(auditlogPort.create(
							nameCmd.actorRef(),
							nameCmd.actorType(),
							nameCmd.severity(),
							nameCmd.originSystem(),
							nameCmd.originService(),
							nameCmd.originComponent(),
							nameCmd.data(),
							nameCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED), eq(nameCmd.name()))
							 )
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
				}
	
				@Test
				void updateName_shouldCompensateUpdateEducationLineName_whenAuditlogCreationFails() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							"PREVIOUS_NAME",
							cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(),
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenReturn(nameResponse);
					when(auditlogPort.create(
							nameCmd.actorRef(),
							nameCmd.actorType(),
							nameCmd.severity(),
							nameCmd.originSystem(),
							nameCmd.originService(),
							nameCmd.originComponent(),
							nameCmd.data(),
							nameCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME")))
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
	
					verify(educationLinePort).compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME"));
				}
	
				@Test
				void updateName_shouldHandleNullCompensationResult_whenCompensateReturnsNull() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							"PREVIOUS_NAME",
							cmd.durationYears(),
							cmd.durationMonths(),
							cmd.durationDays(),
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenReturn(nameResponse);
					when(auditlogPort.create(
							nameCmd.actorRef(),
							nameCmd.actorType(),
							nameCmd.severity(),
							nameCmd.originSystem(),
							nameCmd.originService(),
							nameCmd.originComponent(),
							nameCmd.data(),
							nameCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME")))
							.thenReturn(null);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
	
					verify(educationLinePort).compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq("PREVIOUS_NAME"));
				}
	
				@Test
				void updateName_shouldThrowDomainException_whenAuditlogCreationFailsWithDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"auditlog.creation.failed");
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(nameResponse);
					when(auditlogPort.create(
							nameCmd.actorRef(),
							nameCmd.actorType(),
							nameCmd.severity(),
							nameCmd.originSystem(),
							nameCmd.originService(),
							nameCmd.originComponent(),
							nameCmd.data(),
							nameCmd.description())).thenThrow(domainException);
	
					DomainException thrown = assertThrows(DomainException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
					assertSame(domainException, thrown);
				}
	
				@Test
				void updateName_shouldHandleNullAuditlogCompensationResult_whenAuditlogCompensateReturnsNull() {
					when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(nameResponse);
					when(auditlogPort.create(
							nameCmd.actorRef(),
							nameCmd.actorType(),
							nameCmd.severity(),
							nameCmd.originSystem(),
							nameCmd.originService(),
							nameCmd.originComponent(),
							nameCmd.data(),
							nameCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED), eq(nameCmd.name())))
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineName(educationLineId, nameCmd));
	
					// When auditlogPort.create() throws, auditlogId stays null, so no auditlog
					// compensation is called
					verify(educationLinePort).compensateUpdateName(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED), eq(nameCmd.name()));
					verify(auditlogPort, never()).compensate(any(), any(), any());
				}
			}
	
			@Nested
			class UpdateEducationLineDuration {
				@Test
				void updateDuration_shouldThrowRuntimeException_whenAuditlogCreationFails() {
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(durationResponse);
					when(auditlogPort.create(
							durationCmd.actorRef(),
							durationCmd.actorType(),
							durationCmd.severity(),
							durationCmd.originSystem(),
							durationCmd.originService(),
							durationCmd.originComponent(),
							durationCmd.data(),
							durationCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateDuration(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq(durationCmd.durationYears()), eq(durationCmd.durationMonths()), eq(durationCmd.durationDays())))
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
				}
	
				@Test
				void updateDuration_shouldCompensateUpdateEducationLineDuration_whenAuditlogCreationFails() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							cmd.name(),
							1,
							2,
							3,
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenReturn(durationResponse);
					when(auditlogPort.create(
							durationCmd.actorRef(),
							durationCmd.actorType(),
							durationCmd.severity(),
							durationCmd.originSystem(),
							durationCmd.originService(),
							durationCmd.originComponent(),
							durationCmd.data(),
							durationCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateDuration(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3)))
							.thenReturn(compensatedSuccess);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
	
					verify(educationLinePort).compensateUpdateDuration(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3));
				}
	
				@Test
				void updateDuration_shouldHandleNullCompensationResult_whenCompensateReturnsNull() {
					EducationLineResponse previousResponse = new EducationLineResponse(
							educationLineId,
							cmd.name(),
							1,
							2,
							3,
							cmd.educationRef(),
							Instant.now(),
							true);
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId))
							.thenReturn(previousResponse)
							.thenReturn(durationResponse);
					when(auditlogPort.create(
							durationCmd.actorRef(),
							durationCmd.actorType(),
							durationCmd.severity(),
							durationCmd.originSystem(),
							durationCmd.originService(),
							durationCmd.originComponent(),
							durationCmd.data(),
							durationCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateUpdateDuration(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
							eq(1),
							eq(2), eq(3)))
							.thenReturn(null);
	
					assertThrows(ConflictException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
	
					verify(educationLinePort).compensateUpdateDuration(eq(educationLineId), any(),
							eq(SagaOutcome.COMPENSATED),
							eq(1), eq(2), eq(3));
				}
	
				@Test
				void updateDuration_shouldThrowDomainException_whenAuditlogCreationFailsWithDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"auditlog.creation.failed");
					when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
							durationCmd.durationMonths(), durationCmd.durationDays()))
							.thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(durationResponse);
					when(auditlogPort.create(
							durationCmd.actorRef(),
							durationCmd.actorType(),
							durationCmd.severity(),
							durationCmd.originSystem(),
							durationCmd.originService(),
							durationCmd.originComponent(),
							durationCmd.data(),
							durationCmd.description())).thenThrow(domainException);
	
					DomainException thrown = assertThrows(DomainException.class,
							() -> service.updateEducationLineDuration(educationLineId, durationCmd));
					assertSame(domainException, thrown);
				}
			}
	
			@Nested
			class ActivateEducationLine {
				@Test
				void activate_shouldThrowConflictException_whenAuditlogCreationFails() {
					when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
	
					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
				}
	
				@Test
				void activate_shouldCompensateEducationLine_whenAuditlogCreationFails() {
					when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));

					when(educationLinePort.compensateActivate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);
					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
	
					verify(educationLinePort).compensateActivate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
					verify(auditlogPort, never()).compensate(any(), any(), any());
				}
	
				@Test
				void activate_shouldHandleNullCompensationResult_whenCompensateReturnsNull() {
					when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
	
					assertThrows(ConflictException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
				}
	
				@Test
				void activate_shouldThrowDomainException_whenAuditlogCreationFailsWithDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"auditlog.creation.failed");
					when(educationLinePort.activateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(domainException);
	
					DomainException thrown = assertThrows(DomainException.class,
							() -> service.activateEducationLine(educationLineId, auditlogCmd));
					assertSame(domainException, thrown);
				}
			}
	
			@Nested
			class DeactivateEducationLine {
	
				@Test
				void deactivate_shouldThrowConflictException_whenAuditlogCreationFails() {
					when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
	
					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				}
	
				@Test
				void deactivate_shouldCompensateEducationLine_whenAuditlogCreationFails() {
					when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
					when(educationLinePort.compensateDeactivate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
							.thenReturn(compensatedSuccess);
					
					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
	
					verify(educationLinePort).compensateDeactivate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
					// Auditlog compensation is not called since auditlogId is null when create()
					// throws
					verify(auditlogPort, never()).compensate(any(), any(), any());
				}
	
				@Test
				void deactivate_shouldHandleNullCompensationResult_whenCompensateReturnsNull() {
					when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(new RuntimeException("auditlog.creation.failed"));
	
					assertThrows(ConflictException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
				}
	
				@Test
				void deactivate_shouldThrowDomainException_whenAuditlogCreationFailsWithDomainException() {
					DomainException domainException = new DomainException(ErrorCode.CONFLICT,
							"auditlog.creation.failed");
					when(educationLinePort.deactivateEducationLine(educationLineId)).thenReturn(educationLineId);
					when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(deactivatedResponse);
					when(auditlogPort.create(
							auditlogCmd.actorRef(),
							auditlogCmd.actorType(),
							auditlogCmd.severity(),
							auditlogCmd.originSystem(),
							auditlogCmd.originService(),
							auditlogCmd.originComponent(),
							auditlogCmd.data(),
							auditlogCmd.description())).thenThrow(domainException);
	
					DomainException thrown = assertThrows(DomainException.class,
							() -> service.deactivateEducationLine(educationLineId, auditlogCmd));
					assertSame(domainException, thrown);
				}
			}
	
			@Nested
			class CompensationEdgeCases {
				@Test
				void create_shouldCompensateAuditlog_whenAuditlogLoggingFailsAfterCreateReturnsId() {
					Logger mockLogger = mock(Logger.class);
					Logger originalLogger = swapStaticLogger(mockLogger);
	
					try {
						when(educationPort.existsById(cmd.educationRef())).thenReturn(educationResponse);
						when(educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
								cmd.durationMonths(),
								cmd.durationDays(), cmd.educationRef()))
								.thenReturn(educationLineId);
						when(educationLinePort.findEducationLineById(educationLineId)).thenReturn(response);
						when(auditlogPort.create(
								cmd.actorRef(),
								cmd.actorType(),
								cmd.severity(),
								cmd.originSystem(),
								cmd.originService(),
								cmd.originComponent(),
								cmd.data(),
								cmd.description())).thenReturn(auditlogId);
						when(educationLinePort.compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED)))
								.thenReturn(compensatedSuccess);
						when(auditlogPort.compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED)))
								.thenReturn(compensatedSuccess);
						doThrow(new RuntimeException("auditlog.logging.failed"))
								.when(mockLogger).info(anyString(), any(), any());
	
						assertThrows(ConflictException.class, () -> service.createEducationLine(cmd));
	
						verify(auditlogPort).compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED));
						verify(educationLinePort).compensate(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED));
					} finally {
						swapStaticLogger(originalLogger);
					}
				}
	
				@Test
				void updateName_shouldCompensateAuditlog_whenAuditlogLoggingFailsAfterCreateReturnsId() {
					Logger mockLogger = mock(Logger.class);
					Logger originalLogger = swapStaticLogger(mockLogger);
	
					try {
						EducationLineResponse previousResponse = new EducationLineResponse(
								educationLineId,
								"PREVIOUS_NAME",
								cmd.durationYears(),
								cmd.durationMonths(),
								cmd.durationDays(),
								cmd.educationRef(),
								Instant.now(),
								true);
						when(educationLinePort.updateEducationLineName(educationLineId, nameCmd.name()))
								.thenReturn(educationLineId);
						when(educationLinePort.findEducationLineById(educationLineId))
								.thenReturn(previousResponse)
								.thenReturn(nameResponse);
						when(auditlogPort.create(
								nameCmd.actorRef(),
								nameCmd.actorType(),
								nameCmd.severity(),
								nameCmd.originSystem(),
								nameCmd.originService(),
								nameCmd.originComponent(),
								nameCmd.data(),
								nameCmd.description())).thenReturn(auditlogId);
						when(educationLinePort.compensateUpdateName(eq(educationLineId), any(), eq(SagaOutcome.COMPENSATED),
								eq("PREVIOUS_NAME")))
								.thenReturn(compensatedSuccess);
						when(auditlogPort.compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED)))
								.thenReturn(compensatedSuccess);
						doThrow(new RuntimeException("auditlog.logging.failed"))
								.when(mockLogger).info(anyString(), any(), any());
	
						assertThrows(ConflictException.class,
								() -> service.updateEducationLineName(educationLineId, nameCmd));
	
						verify(auditlogPort).compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED));
						verify(educationLinePort).compensateUpdateName(eq(educationLineId), any(),
								eq(SagaOutcome.COMPENSATED),
								eq("PREVIOUS_NAME"));
					} finally {
						swapStaticLogger(originalLogger);
					}
				}
	
				@Test
				void updateDuration_shouldCompensateAuditlog_whenAuditlogLoggingFailsAfterCreateReturnsId() {
					Logger mockLogger = mock(Logger.class);
					Logger originalLogger = swapStaticLogger(mockLogger);
	
					try {
						EducationLineResponse previousResponse = new EducationLineResponse(
								educationLineId,
								cmd.name(),
								1,
								2,
								3,
								cmd.educationRef(),
								Instant.now(),
								true);
						when(educationLinePort.updateEducationLineDuration(educationLineId, durationCmd.durationYears(),
								durationCmd.durationMonths(), durationCmd.durationDays()))
								.thenReturn(educationLineId);
						when(educationLinePort.findEducationLineById(educationLineId))
								.thenReturn(previousResponse)
								.thenReturn(durationResponse);
						when(auditlogPort.create(
								durationCmd.actorRef(),
								durationCmd.actorType(),
								durationCmd.severity(),
								durationCmd.originSystem(),
								durationCmd.originService(),
								durationCmd.originComponent(),
								durationCmd.data(),
								durationCmd.description())).thenReturn(auditlogId);
						when(educationLinePort.compensateUpdateDuration(eq(educationLineId), any(),
								eq(SagaOutcome.COMPENSATED),
								eq(1), eq(2), eq(3)))
								.thenReturn(compensatedSuccess);
						when(auditlogPort.compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED)))
								.thenReturn(compensatedSuccess);
						doThrow(new RuntimeException("auditlog.logging.failed"))
								.when(mockLogger).info(anyString(), any(), any());
	
						assertThrows(ConflictException.class,
								() -> service.updateEducationLineDuration(educationLineId, durationCmd));
	
						verify(auditlogPort).compensate(eq(auditlogId), any(), eq(SagaOutcome.COMPENSATED));
						verify(educationLinePort).compensateUpdateDuration(eq(educationLineId), any(),
								eq(SagaOutcome.COMPENSATED),
								eq(1), eq(2), eq(3));
					} finally {
						swapStaticLogger(originalLogger);
					}
				}
	
			}
	
			private static Logger swapStaticLogger(Logger newLogger) {
				try {
					Field loggerField = EducationLineSagaApplicationService.class.getDeclaredField("log");
					loggerField.setAccessible(true);
	
					Logger previousLogger = (Logger) loggerField.get(null);
					loggerField.set(null, newLogger);
					return previousLogger;
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}
}
