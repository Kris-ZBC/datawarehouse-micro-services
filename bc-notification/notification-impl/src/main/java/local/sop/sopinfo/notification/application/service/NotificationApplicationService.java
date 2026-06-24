package local.sop.sopinfo.notification.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.notification.application.api.NotificationDirectory;
import local.sop.sopinfo.notification.application.api.dto.CreateNotificationCmd;
import local.sop.sopinfo.notification.application.api.dto.NotificationResponse;
import local.sop.sopinfo.notification.domain.model.Notification;
import local.sop.sopinfo.notification.domain.ports.out.NotificationRepositoryPort;
import local.sop.sopinfo.notification.domain.service.NotificationDomain;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Service
public class NotificationApplicationService implements NotificationDirectory {
	private final NotificationRepositoryPort repo;
	private final NotificationDomain domain;
	private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

	NotificationApplicationService(NotificationRepositoryPort repo, NotificationDomain domain) {
		this.repo = repo;
		this.domain = domain;
	}

	@Transactional
	@Override
	public UUID createNotification(CreateNotificationCmd cmd) {
		try {
			Notification notification = domain.createNotification(cmd.messageRef());
			notification = repo.save(notification);
			log.info("Notification created {}", notification);
			return notification.getId().value();
		}
		catch(ValidationException ex) {
			throw ex;
		}
		catch(RuntimeException ex) {
			log.error("Error in createNotification", ex);
			throw new ValidationException("notification.create.failed", Map.of("function", "createNotification"));
		}
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<NotificationResponse> findById(UUID id) {
		try {
        return repo.findById(id)
            .map(n -> new NotificationResponse(
                n.getId().value(),
                n.getSeen(),
                n.getCreatedAt().value(),
                n.getMessageRef().value()
            ));
		}
		catch(ValidationException ex) {
			throw ex;
		}
		catch(RuntimeException ex) {
			log.error("Error in findById", ex);
			throw new ValidationException("notification.find_by_id.failed", Map.of("function", "findById"));
		}
	}

	@Transactional
	@Override
	public void makeNotificationSeen(UUID id) {
		try {
			repo.makeNotificationSeen(id);
			log.info("Notification with id {} marked as seen", id);
		}
		catch(ValidationException ex) {
			throw ex;
		}
		catch(RuntimeException ex) {
			log.error("Error in makeNotificationSeen", ex);
			throw new ValidationException("notification.make_seen.failed", Map.of("function", "makeNotificationSeen"));
		}
	}

	@Transactional
	@Override
	public void deleteNotification(UUID id) {
		try {
			repo.deleteById(id);
			log.info("Notification with id {} deleted", id);
		}
		catch(ValidationException ex) {
			throw ex;
		}
		catch(RuntimeException ex) {
			log.error("Error in deleteNotification", ex);
			throw new ValidationException("notification.delete.failed", Map.of("function", "deleteNotification"));
		}
	}
}
