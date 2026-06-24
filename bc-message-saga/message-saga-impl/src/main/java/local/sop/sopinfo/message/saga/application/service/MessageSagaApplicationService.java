package local.sop.sopinfo.message.saga.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import local.sop.sopinfo.message.saga.application.api.MessageSagaDirectory;
import local.sop.sopinfo.message.saga.application.api.dto.ApprenticeResponse;
import local.sop.sopinfo.message.saga.application.api.dto.AuditLogResponse;
import local.sop.sopinfo.message.saga.application.api.dto.CreateAuditLogCmd;
import local.sop.sopinfo.message.saga.application.api.dto.CreateMessageCmd;
import local.sop.sopinfo.message.saga.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.message.saga.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.message.saga.application.api.dto.InstructorResponse;
import local.sop.sopinfo.message.saga.application.api.dto.MessageResponse;
import local.sop.sopinfo.message.saga.application.api.dto.NotificationResponse;
import local.sop.sopinfo.message.saga.application.api.dto.PersonNotificationResponse;
import local.sop.sopinfo.message.saga.application.api.dto.MessagePersonResponse;
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
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.exceptions.DomainException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;
import local.sop.sopinfo.sharedkernel.sagas.concurrency.locks.SagaStatus;

@Service
public class MessageSagaApplicationService implements MessageSagaDirectory {

	private static final Logger log = LoggerFactory.getLogger(MessageSagaApplicationService.class);

	private final MessagePort messages;
	private final NotificationPort notifications;
	private final ApprenticePort apprentices;
	private final InstructorPort instructors;
	private final MessagePersonPort messagepersons;
	private final AuditLogPort auditlogs;
	private final EducationInstructorPort educationinstructors;
	private final EducationLinePort educationlines;
	private final PersonPort persons;
	private final PersonNotificationPort personnotifications;
	private final MessageSagaStatePort sagaLock;

	public MessageSagaApplicationService(MessagePort messages, NotificationPort notifications,
			ApprenticePort apprentices,
			InstructorPort instructors, MessagePersonPort messagepersons, AuditLogPort auditlogs,
			EducationInstructorPort educationinstructors, EducationLinePort educationlines, PersonPort persons,
			PersonNotificationPort personnotifications, MessageSagaStatePort sagaLock) {
		this.messages = messages;
		this.notifications = notifications;
		this.apprentices = apprentices;
		this.instructors = instructors;
		this.messagepersons = messagepersons;
		this.auditlogs = auditlogs;
		this.educationinstructors = educationinstructors;
		this.educationlines = educationlines;
		this.persons = persons;
		this.personnotifications = personnotifications;
		this.sagaLock = sagaLock;
	}

	/*
	 * 1. Acquire session lock (concurrency guard)
	 * 2. Create message
	 * 3. Message is readable (sanity)
	 * 4. Get education lines with educationRef
	 * 5. Get apprentices with education line
	 * 6. Attach apprentices to message
	 * 7. Get instructors with education
	 * 8. Get instructors with instructorRef
	 * 9. Find all personRef (sanity)
	 * 10. Attach all persons to message
	 * 11. Find all persons attached to message (sanity)
	 * 12. Create notification
	 * 13. Find notification (sanity)
	 * 14. Attach all persons to notification
	 * 15. Find all persons attached to notification (sanity)
	 * 16. Create audit log
	 * 17. Get auditLog (sanity)
	 * 18. Release lock
	 */

	@Override
	@Transactional
	public MessageResponse createMessage(CreateMessageCmd cmd) {
		UUID sessionId = cmd.sessionId();

		// 1. Acquire session lock
		if (!sagaLock.tryLock(SagaConcurrencyLock.start(sessionId))) {
			throw new ConflictException("saga.already.running",
					Map.of("sessionId", sessionId.toString()));
		}

		log.info("SAGA[{}]: lock acquired, starting createMessage", sessionId);

		// 2. Create message
		MessageResponse message;
		try {
			message = messages.create(cmd.senderPersonRef(), cmd.message());
			log.info("SAGA[{}]: message created {}", sessionId, message.id());
		} catch (DomainException ex) {
			sagaLock.release(sessionId);
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.release(sessionId);
			log.warn("SAGA [{}]: create message failed", sessionId);
			throw new ConflictException("message.not.created", Map.of("object", "message"));
		}

		// 3. Message is readable (sanity)
		try {
			MessageResponse createdMessage = messages.findById(message.id());
			if (createdMessage == null) {
				sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
				compensateMessage(sessionId, message.id());
				throw new ConflictException("message.not.found.after.creation", Map.of("id", message.id()));
			}
			log.info("SAGA[{}]: message sanity OK {}", sessionId, message.id());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			log.warn("SAGA [{}]: message sanity check failed for {}, compensating", sessionId, message.id());
			compensateMessage(sessionId, message.id());
			throw new ConflictException("message.not.found.after.creation", Map.of("id", message.id()));
		}

		try {
			// 4. Get education lines with educationRef
			List<EducationLineResponse> educationLineList = educationlines.findByEducationRef(cmd.educationRef());
			// 5. Get apprentices with education line
			for (EducationLineResponse educationLine : educationLineList) {
				List<ApprenticeResponse> apprenticeList = apprentices.findByEducationLineRef(educationLine.id());
				// 6. Attach apprentices to message
				for (ApprenticeResponse apprentice : apprenticeList) {
					messagepersons.create(apprentice.personRef(), message.id());
				}
			}
			log.info("SAGA[{}]: attached apprentices to message {}", sessionId, message.id());
		} catch (DomainException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("message.not.updated", Map.of("object", "message"));
		}

		try {
			// 7. Get instructors with education
			List<EducationInstructorResponse> educationInstructorList = educationinstructors.findByEducationRef(cmd.educationRef());
			// 8. Get instructors with instructorRef
			for (EducationInstructorResponse educationInstructor : educationInstructorList) {
				UUID instructorRef = educationInstructor.id().keys()[1];
				InstructorResponse instructor = instructors.findById(instructorRef);
				// 9. Find all personRef (sanity)
				persons.findById(instructor.personRef());
				// 10. Attach all persons to message
				messagepersons.create(instructor.personRef(), message.id());
			}
			log.info("SAGA[{}]: attached instructors to message {}", sessionId, message.id());
		} catch (DomainException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("message.not.updated", Map.of("object", "message"));
		}

		// 11. Find all persons attached to message (sanity)
		List<MessagePersonResponse> recepients;
		try {
			recepients = messagepersons.findByMessageRef(message.id());
			if (recepients == null) {
				sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
				compensateMessage(sessionId, message.id());
				throw new ConflictException("message.persons.not.found.after.creation", Map.of("id", message.id()));
			}
			log.info("SAGA[{}]: message recipients sanity OK {}", sessionId, message.id());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("message.persons.not.found.after.creation", Map.of("messageId", message.id()));
		}

		// 12. Create notification
		UUID notificationId;
		try {
			notificationId = notifications.create(message.id());
			log.info("SAGA[{}]: notification created {}", sessionId, notificationId);
		} catch (DomainException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateMessage(sessionId, message.id());
			log.warn("SAGA[{}]: notification creation failed", sessionId);
			throw new ConflictException("notification.not.created", Map.of("object", "notification"));
		}

		// 13. Find notification (sanity)
		try {
			NotificationResponse createdNotification = notifications.findById(notificationId);
			if (createdNotification == null) {
				sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
				compensateNotification(sessionId, notificationId);
				compensateMessage(sessionId, message.id());
				throw new ConflictException("notification.not.found.after.creation", Map.of("id", notificationId));
			}
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("notification.read.failed", Map.of("id", notificationId));
		}

		// 14. Attach all persons to notification
		try {
			for (MessagePersonResponse recepient : recepients) {
				UUID personRef = recepient.id().keys()[0];
				personnotifications.create(personRef, notificationId);
			}
			log.info("SAGA[{}]: attached persons to notification {}", sessionId, notificationId);
		} catch (DomainException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("notification.not.created", Map.of("object", "notification"));
		}

		// 15. Find all persons attached to notification (sanity)
		List<PersonNotificationResponse> notificationRecepients;
		try {
			notificationRecepients = personnotifications.findByNotificationRef(notificationId);
			if (notificationRecepients == null) {
				sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
				compensateNotification(sessionId, notificationId);
				compensateMessage(sessionId, message.id());
				throw new ConflictException("person.notifications.not.found.after.creation",
						Map.of("notificationId", notificationId));
			}
			log.info("SAGA[{}]: notification recipients sanity OK {}", sessionId, notificationId);
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("person.notifications.not.found.after.creation",
					Map.of("notificationId", notificationId));
		}

		// 16. Create audit log
		UUID auditlogId;
		try {
			auditlogId = auditlogs.create(new CreateAuditLogCmd(
					cmd.senderPersonRef(),
					cmd.actorType(),
					cmd.severity(),
					cmd.originSystem(),
					cmd.originService(),
					cmd.originComponent(),
					cmd.data(),
					cmd.description()));
			log.info("SAGA[{}]: audit log created {}", sessionId, auditlogId);
		} catch (DomainException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			log.warn("SAGA[{}]: audit log creation failed, compensating", sessionId);
			throw new ConflictException("auditlog.not.created", Map.of("object", "audit log"));
		}

		// 17. Get auditLog (sanity)
		try {
			AuditLogResponse auditlog = auditlogs.getById(auditlogId);
			if (auditlog == null) {
				log.warn("SAGA[{}]: sanity check for auditlog failed, compensating", sessionId);
				sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
				compensateAuditLog(sessionId, auditlogId);
				compensateNotification(sessionId, notificationId);
				compensateMessage(sessionId, message.id());
				throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
			}
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
			compensateAuditLog(sessionId, auditlogId);
			compensateNotification(sessionId, notificationId);
			compensateMessage(sessionId, message.id());
			throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
		}

		// 18. Release lock
		sagaLock.release(sessionId);
		log.info("SAGA[{}]: completed, lock release. messageId={}, notificationId={}", sessionId, message.id(), notificationId);
		
		return message;
	}

	// helper methods
	private void compensateMessage(UUID sessionId, UUID messageId) {
		try {
			var result = messages.compensate(messageId, getClass(), SagaOutcome.COMPENSATED);
			if (result.success()) {
				log.info("SAGA [{}]: message {} compensated successfully", sessionId, messageId);
			} else {
				log.error("SAGA [{}]: compensation of message {} FAILED — manual intervention required", sessionId,
						messageId);
			}
		} catch (RuntimeException ex) {
			log.error("SAGA [{}]: compensation of message {} threw exception — manual intervention required: {}",
					sessionId, messageId, ex.getMessage());
		} finally {
			sagaLock.release(sessionId);
		}
	}

	private void compensateNotification(UUID sessionId, UUID notificationId) {
		try {
			var result = notifications.compensate(notificationId, getClass(), SagaOutcome.COMPENSATED);
			if (result.success()) {
				log.info("SAGA [{}]: notification {} compensated successfully", sessionId, notificationId);
			} else {
				log.error("SAGA [{}]: compensation of notification {} FAILED — manual intervention required", sessionId, notificationId);
			}
		} catch (RuntimeException ex) {
			log.error("SAGA [{}]: compensation of notification {} threw exception — manual intervention required: {}",
					sessionId, notificationId, ex.getMessage());
		} finally {
			sagaLock.release(sessionId);
		}
	}

	private void compensateAuditLog(UUID sessionId, UUID auditLogId) {
		try {
			var result = auditlogs.compensate(auditLogId, getClass(), SagaOutcome.COMPENSATED);
			if (result.success()) {
				log.info("SAGA [{}]: audit log {} compensated successfully", sessionId, auditLogId);
			} else {
				log.error("SAGA [{}]: compensation of audit log {} FAILED — manual intervention required", sessionId,
						auditLogId);
			}
		} catch (RuntimeException ex) {
			log.error("SAGA [{}]: compensation of audit log {} threw exception — manual intervention required: {}",
					sessionId, auditLogId, ex.getMessage());
		}
	}
}
