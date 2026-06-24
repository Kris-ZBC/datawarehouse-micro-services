package local.sop.sopinfo.message.saga.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.message.saga.application.api.dto.ApprenticeResponse;
import local.sop.sopinfo.message.saga.application.api.dto.AuditLogResponse;
import local.sop.sopinfo.message.saga.application.api.dto.CreateMessageCmd;
import local.sop.sopinfo.message.saga.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.message.saga.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.message.saga.application.api.dto.InstructorResponse;
import local.sop.sopinfo.message.saga.application.api.dto.MessagePersonResponse;
import local.sop.sopinfo.message.saga.application.api.dto.MessageResponse;
import local.sop.sopinfo.message.saga.application.api.dto.NotificationResponse;
import local.sop.sopinfo.message.saga.application.api.dto.PersonNotificationResponse;
import local.sop.sopinfo.message.saga.application.api.dto.PersonResponse;
import local.sop.sopinfo.message.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.sopinfo.message.saga.application.ports.out.auditlog.AuditLogPort;
import local.sop.sopinfo.message.saga.application.ports.out.educationinstructor.EducationInstructorPort;
import local.sop.sopinfo.message.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.sopinfo.message.saga.application.ports.out.instructor.InstructorPort;
import local.sop.sopinfo.message.saga.application.ports.out.message.MessagePort;
import local.sop.sopinfo.message.saga.application.ports.out.messageperson.MessagePersonPort;
import local.sop.sopinfo.message.saga.application.ports.out.notification.NotificationPort;
import local.sop.sopinfo.message.saga.application.ports.out.person.PersonPort;
import local.sop.sopinfo.message.saga.application.ports.out.personnotification.PersonNotificationPort;
import local.sop.sopinfo.message.saga.application.ports.out.saga.MessageSagaStatePort;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.exceptions.DomainException;
import local.sop.sopinfo.sharedkernel.exceptions.ErrorCode;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
public class MessageSagaApplicationServiceTest {

	@Mock
	private MessagePort messages;

	@Mock
	private NotificationPort notifications;

	@Mock
	private ApprenticePort apprentices;

	@Mock
	private InstructorPort instructors;

	@Mock
	private MessagePersonPort messagepersons;

	@Mock
	private AuditLogPort auditlogs;

	@Mock
	private EducationInstructorPort educationinstructors;

	@Mock
	private EducationLinePort educationlines;

	@Mock
	private PersonPort persons;

	@Mock
	private PersonNotificationPort personnotifications;

	@Mock
	private MessageSagaStatePort sagaLock;

	private MessageSagaApplicationService service;

	private UUID messageId;
	private UUID sessionId;
	private UUID auditlogId;
	private UUID educationRef;
	private UUID educationLineId;
	private UUID instructorId;
	private UUID senderPersonRef;
	private UUID apprenticePersonRef;
	private UUID instructorPersonRef;
	private UUID notificationId;

	private CreateMessageCmd createCmd;
	private MessageResponse messageResponse;
	private NotificationResponse notificationResponse;
	private AuditLogResponse auditLogResponse;
	private List<EducationLineResponse> educationLineList;
	private List<ApprenticeResponse> apprenticeList;
	private List<EducationInstructorResponse> educationInstructorList;
	private List<MessagePersonResponse> recipientList;
	private List<PersonNotificationResponse> personNotificationList;
	private ResponseCompensated compensatedOk;

	@BeforeEach
	void setUp() {
		service = new MessageSagaApplicationService(messages, notifications, apprentices, instructors, messagepersons,
				auditlogs, educationinstructors, educationlines, persons, personnotifications, sagaLock);

		messageId = UUID.randomUUID();
		sessionId = UUID.randomUUID();
		auditlogId = UUID.randomUUID();
		educationRef = UUID.randomUUID();
		educationLineId = UUID.randomUUID();
		instructorId = UUID.randomUUID();
		senderPersonRef = UUID.randomUUID();
		apprenticePersonRef = UUID.randomUUID();
		instructorPersonRef = UUID.randomUUID();
		notificationId = UUID.randomUUID();

		createCmd = new CreateMessageCmd(sessionId, senderPersonRef, educationRef, "Message test", senderPersonRef,
				ActorType.USER, Severity.INFO,
				"system", "service", "component", "data", "description");

		messageResponse = new MessageResponse(messageId, OffsetDateTime.now(), "Message test", senderPersonRef);
		notificationResponse = new NotificationResponse(notificationId, false, LocalDateTime.now(), messageId);
		auditLogResponse = new AuditLogResponse(auditlogId, senderPersonRef, ActorType.USER, Severity.INFO,
				"system", "service", "component", "data", "description", Instant.now());
		educationLineList = List.of(
				new EducationLineResponse(educationLineId, "Line A", 1, 0, 0, educationRef, Instant.now(), true));
		apprenticeList = List.of(
				new ApprenticeResponse(UUID.randomUUID(), apprenticePersonRef, educationLineId));
		educationInstructorList = List.of(
				new EducationInstructorResponse(new CompositeKey(educationRef, instructorId), Instant.now(), true));
		recipientList = List.of(
				new MessagePersonResponse(new CompositeKey(apprenticePersonRef, messageId), true, LocalDateTime.now()),
				new MessagePersonResponse(new CompositeKey(instructorPersonRef, messageId), true, LocalDateTime.now()));
		personNotificationList = List.of(
				new PersonNotificationResponse(UUID.randomUUID(), apprenticePersonRef, notificationId));

		compensatedOk = new ResponseCompensated(SagaOutcome.COMPENSATED, true);
	}

	@Nested
	class SessionLock {

		@Test
		void createMessage_shouldThrowConflictException_whenLockAlreadyExists() {
			when(sagaLock.tryLock(any())).thenReturn(false);

			ConflictException ex = assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			assertEquals("saga.already.running", ex.getMessage());
		}

		@Test
		void createMessage_shouldNeverCallMessages_whenLockAlreadyExists() {
			when(sagaLock.tryLock(any())).thenReturn(false);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages, never()).create(any(), any());
		}

		@Test
		void createMessage_shouldAcquireLockWithSessionId() {
			when(sagaLock.tryLock(any())).thenReturn(false);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(sagaLock).tryLock(argThat(lock -> lock.sessionId().equals(sessionId)));
		}

		@Test
		void createMessage_shouldReleaseLock_whenAllStepsSucceded() {
			allStepsHappyPath();

			service.createMessage(createCmd);

			verify(sagaLock).release(sessionId);
		}

		@Test
		void createMessage_shouldReleaseLock_whenCreateMessageFails() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenThrow(new RuntimeException("downstream unavailable"));

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(sagaLock).release(sessionId);
		}

		@Test
		void createMessage_shouldReleaseLock_afterCompensation() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(null);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(sagaLock).release(sessionId);
		}

		@Test
		void createMessage_shouldReleaseLock_evenWhenCompensationFails() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(null);
			when(messages.compensate(any(), any(), any())).thenThrow(new RuntimeException("compensation failed"));

			assertThrows(RuntimeException.class, () -> service.createMessage(createCmd));

			verify(sagaLock).release(sessionId);
		}
	}

	@Nested
	class HappyPath {

		@BeforeEach
		void lockAcquired() {
			when(sagaLock.tryLock(any())).thenReturn(true);
		}

		@Test
		void createMessage_shouldReturnMessageResponse_whenAllStepsSucceed() {
			allStepsHappyPath();

			MessageResponse result = service.createMessage(createCmd);

			assertNotNull(result);
			assertEquals(messageResponse.id(), result.id());
		}

		@Test
		void createMessage_shouldNeverCallCompensation_whenAllStepsSucceed() {
			allStepsHappyPath();

			service.createMessage(createCmd);

			verify(messages, never()).compensate(any(), any(), any());
			verify(notifications, never()).compensate(any(), any(), any());
			verify(auditlogs, never()).compensate(any(), any(), any());
		}

		@Test
		void createMessage_shouldInvokeAllStepsInOrder() {
			allStepsHappyPath();

			service.createMessage(createCmd);

			var inOrder = inOrder(sagaLock, messages, educationlines, apprentices, educationinstructors, instructors,
					persons, messagepersons, notifications, personnotifications, auditlogs);
			inOrder.verify(sagaLock).tryLock(any());
			inOrder.verify(messages).create(senderPersonRef, createCmd.message());
			inOrder.verify(messages).findById(messageId);
			inOrder.verify(educationlines).findByEducationRef(educationRef);
			inOrder.verify(apprentices).findByEducationLineRef(educationLineId);
			inOrder.verify(educationinstructors).findByEducationRef(educationRef);
			inOrder.verify(instructors).findById(instructorId);
			inOrder.verify(persons).findById(instructorPersonRef);
			inOrder.verify(messagepersons).findByMessageRef(messageId);
			inOrder.verify(notifications).create(messageId);
			inOrder.verify(notifications).findById(notificationId);
			inOrder.verify(personnotifications).findByNotificationRef(notificationId);
			inOrder.verify(auditlogs).create(any());
			inOrder.verify(auditlogs).getById(auditlogId);
			inOrder.verify(sagaLock).release(sessionId);
		}
	}

	// Step 2: message create failures
	@Nested
	class MessageCreateFailure {
		@BeforeEach
		void lockAcquired() {
			when(sagaLock.tryLock(any())).thenReturn(true);
		}

		@Test
		void shouldThrowConflictException_whenCreateMessageThrowsRuntimeException() {
			when(messages.create(any(), any())).thenThrow(new RuntimeException("downstream unavailable"));

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));
		}

		@Test
		void shouldNotProceedToGetOrAuditLog_whenCreateMessageFails() {
			when(messages.create(any(), any())).thenThrow(new RuntimeException("downstream unavailable"));

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages, never()).findById(any());
			verify(auditlogs, never()).create(any());

		}

		@Test
		void shouldNotCompensate_whenCreateMessageFails() {
			when(messages.create(any(), any())).thenThrow(new RuntimeException("downstream unavailable"));

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages, never()).compensate(any(), any(), any());
			verify(notifications, never()).compensate(any(), any(), any());
			verify(auditlogs, never()).compensate(any(), any(), any());
		}

		@Test
		void shouldThrowDomainException_whenCreateMessageThrowsDomainException() {
			var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
					Map.of("id", messageId.toString()));
			when(messages.create(any(), any())).thenThrow(domainEx);

			DomainException thrown = assertThrows(DomainException.class, () -> service.createMessage(createCmd));
			assertSame(domainEx, thrown);
		}

		// Step 3: message sanity check failures
		@Nested
		class MessageSanitCheckFailure {
			@BeforeEach
			void lockAndCreateMessage() {
				when(sagaLock.tryLock(any())).thenReturn(true);
				when(messages.create(any(), any())).thenReturn(messageResponse);
			}

			@Test
			void shouldCompensateMessage_whenFindByIdReturnsNull() {
				when(messages.findById(messageId)).thenReturn(null);
				when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

				assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

				verify(messages).compensate(eq(messageId), eq(MessageSagaApplicationService.class),
						eq(SagaOutcome.COMPENSATED));
			}

			@Test
			void shouldNotProceedToEducationLines_whenSanityFails() {
				when(messages.findById(messageId)).thenReturn(null);
				when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

				assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

				verify(educationlines, never()).findByEducationRef(any());
			}

			@Test
			void shouldThrowConflictException_whenFindByIdThrowsRuntimeException() {
				when(messages.findById(messageId)).thenThrow(new RuntimeException("downstream unavailable"));

				assertThrows(ConflictException.class, () -> service.createMessage(createCmd));
			}
		}
	}

	// Step4-6: apprentice attachment failures
	@Nested
	class ApprenticeAttachmentFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
		}

		@Test
		void shouldCompensateMessage_whenEducationLineFetchThrows() {
			when(educationlines.findByEducationRef(any())).thenThrow(new RuntimeException("downstream unavailable"));
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldNotProceedToInstructors_whenApprenticeAttachmentFails() {
			when(educationlines.findByEducationRef(any())).thenThrow(new RuntimeException("downstream unavailable"));
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(educationinstructors, never()).findByEducationRef(any());
		}

		@Test
		void shouldThrowConflictException_whenFindByEducationRefThrowsRuntimeException() {
			when(educationlines.findByEducationRef(educationRef))
					.thenThrow(new RuntimeException("downstream unavailable"));

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));
		}
	}

	// Step 7-10: instructor attachment failures
	@Nested
	class InstructorAttachmentFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
		}

		@Test
		void shouldCompensateMessage_whenEducationInstructorFetchThrows() {
			when(educationinstructors.findByEducationRef(any()))
					.thenThrow(new RuntimeException("downstream unavailable"));
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateMessage_whenPersonFindThrowsDomainException() {
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			var domainEx = new DomainException(ErrorCode.NOT_FOUND, "person.not.found",
					Map.of("id", instructorPersonRef.toString()));
			when(persons.findById(instructorPersonRef)).thenThrow(domainEx);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(DomainException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 11: recipient sanity failures
	@Nested
	class RecepientsSanityFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
		}

		@Test
		void shouldCompensateMessage_whenRecepientsListIsNull() {
			when(messagepersons.findByMessageRef(messageId)).thenReturn(null);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateMessage_whenFindByMessageRefThrowsRuntimeException() {
			when(messagepersons.findByMessageRef(messageId)).thenThrow(new RuntimeException("downstream unavailable"));
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 12: notification create failures
	@Nested
	class NotificationCreateFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
			when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
		}

		@Test
		void shouldCompensateMessage_whenNotificationCreateThrowsRuntimeException() {
			when(notifications.create(messageId)).thenThrow(new RuntimeException("downstream unavailable"));
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateMessage_whenNotificationCreateThrowsDomainException() {
			var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
					Map.of("id", notificationId.toString()));
			when(notifications.create(messageId)).thenThrow(domainEx);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(DomainException.class, () -> service.createMessage(createCmd));

			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 13: notification sanity failures
	@Nested
	class NotificationSanityFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
			when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
			when(notifications.create(messageId)).thenReturn(notificationId);
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenFindByIdReturnsNull() {
			when(notifications.findById(notificationId)).thenReturn(null);
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenFindByIdThrowsRuntimeException() {
			when(notifications.findById(notificationId)).thenThrow(new RuntimeException("downstream unavailable"));
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 14: person notification create failures
	@Nested
	class PersonNotificationFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
			when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
			when(notifications.create(messageId)).thenReturn(notificationId);
			when(notifications.findById(notificationId)).thenReturn(notificationResponse);
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenPersonNotificationCreateThrowsDomainException() {
			var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
					Map.of("id", notificationId.toString()));
			doThrow(domainEx).when(personnotifications).create(any(), any());
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(DomainException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenPersonNotificationCreateThrows() {
			doThrow(new RuntimeException("downstream")).when(personnotifications).create(any(), any());
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 15: person notification sanity failures
	@Nested
	class PersonNotificationSanityFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
			when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
			when(notifications.create(messageId)).thenReturn(notificationId);
			when(notifications.findById(notificationId)).thenReturn(notificationResponse);
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenFindByNotificationRefReturnsNull() {
			when(personnotifications.findByNotificationRef(notificationId)).thenReturn(null);
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenFindByNotificationRefThrowsRuntimeException() {
			when(personnotifications.findByNotificationRef(notificationId))
					.thenThrow(new RuntimeException("downstream unavailable"));
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 16: audit log failures
	@Nested
	class AuditlogCreateFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
			when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
			when(notifications.create(messageId)).thenReturn(notificationId);
			when(notifications.findById(notificationId)).thenReturn(notificationResponse);
			when(personnotifications.findByNotificationRef(notificationId)).thenReturn(personNotificationList);
		}

		@Test
		void shouldCompensateAuditlogAndMessage_whenAuditLogCreateThrowsRuntimeException() {
			when(auditlogs.create(any())).thenThrow(new RuntimeException("downstream unavailable"));
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}

		@Test
		void shouldCompensateNotificationAndMessage_whenAuditLogCreateThrowsDomainException() {
			var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
					Map.of("id", auditlogId.toString()));
			when(auditlogs.create(any())).thenThrow(domainEx);
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(DomainException.class, () -> service.createMessage(createCmd));

			verify(notifications).compensate(any(), any(), any());
			verify(messages).compensate(any(), any(), any());
		}
	}

	// Step 17: auditlog sanity failures
	@Nested
	class AuditlogSanityFailure {
		@BeforeEach
		void succeedPreviousSteps() {
			when(sagaLock.tryLock(any())).thenReturn(true);
			when(messages.create(any(), any())).thenReturn(messageResponse);
			when(messages.findById(messageId)).thenReturn(messageResponse);
			when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
			when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
			when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
			when(instructors.findById(instructorId))
					.thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
			when(persons.findById(instructorPersonRef)).thenReturn(
					new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
			when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
			when(notifications.create(messageId)).thenReturn(notificationId);
			when(notifications.findById(notificationId)).thenReturn(notificationResponse);
			when(personnotifications.findByNotificationRef(notificationId)).thenReturn(personNotificationList);
			when(auditlogs.create(any())).thenReturn(auditlogId);
		}

		@Test
		void shouldCompensateAll_whenGetByIdReturnsNull() {
			when(auditlogs.getById(auditlogId)).thenReturn(null);
			when(auditlogs.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);
		
			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(auditlogs).compensate(eq(auditlogId), eq(MessageSagaApplicationService.class), eq(SagaOutcome.COMPENSATED));
			verify(notifications).compensate(eq(notificationId), eq(MessageSagaApplicationService.class), eq(SagaOutcome.COMPENSATED));
			verify(messages).compensate(eq(messageId), eq(MessageSagaApplicationService.class), eq(SagaOutcome.COMPENSATED));
		}

		@Test
		void shouldCompensateAll_whenGetByIdThrowsRuntimeException() {
			when(auditlogs.getById(auditlogId)).thenThrow(new RuntimeException("downstream unavailable"));
			when(auditlogs.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(notifications.compensate(any(), any(), any())).thenReturn(compensatedOk);
			when(messages.compensate(any(), any(), any())).thenReturn(compensatedOk);

			assertThrows(ConflictException.class, () -> service.createMessage(createCmd));

			verify(auditlogs).compensate(eq(auditlogId), eq(MessageSagaApplicationService.class), eq(SagaOutcome.COMPENSATED));
			verify(notifications).compensate(eq(notificationId), eq(MessageSagaApplicationService.class), eq(SagaOutcome.COMPENSATED));
			verify(messages).compensate(eq(messageId), eq(MessageSagaApplicationService.class), eq(SagaOutcome.COMPENSATED));
		}
	}

	// helpers
	private void allStepsHappyPath() {
		when(sagaLock.tryLock(any())).thenReturn(true);
		when(messages.create(any(), any())).thenReturn(messageResponse);
		when(messages.findById(messageId)).thenReturn(messageResponse);
		when(educationlines.findByEducationRef(educationRef)).thenReturn(educationLineList);
		when(apprentices.findByEducationLineRef(educationLineId)).thenReturn(apprenticeList);
		when(educationinstructors.findByEducationRef(educationRef)).thenReturn(educationInstructorList);
		when(instructors.findById(instructorId)).thenReturn(new InstructorResponse(instructorId, instructorPersonRef));
		when(persons.findById(instructorPersonRef)).thenReturn(
				new PersonResponse(instructorPersonRef, "John", "Doe", "JohnDoe@zbc.dk", UUID.randomUUID()));
		when(messagepersons.findByMessageRef(messageId)).thenReturn(recipientList);
		when(notifications.create(messageId)).thenReturn(notificationId);
		when(notifications.findById(notificationId)).thenReturn(notificationResponse);
		when(personnotifications.findByNotificationRef(notificationId)).thenReturn(personNotificationList);
		when(auditlogs.create(any())).thenReturn(auditlogId);
		when(auditlogs.getById(auditlogId)).thenReturn(auditLogResponse);
	}
}
