package local.sop.sopinfo.notification.interfaceadapters.persistence.jpa;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import local.sop.sopinfo.notification.domain.model.Notification;
import local.sop.sopinfo.notification.domain.model.valueobjects.NotificationId;
import local.sop.sopinfo.notification.domain.ports.out.NotificationRepositoryPort;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;


@Repository
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {
	private static final Logger log = LoggerFactory.getLogger(NotificationRepositoryAdapter.class);
	private final NotificationMapper mapper;
	private final NotificationSpringDataRepository repo;

	public NotificationRepositoryAdapter(NotificationMapper mapper, NotificationSpringDataRepository repo) {
		this.mapper = mapper;
		this.repo = repo;
	}

	@Override
	public Notification save(Notification notification) {
		NotificationEntity entity = mapper.toEntity(notification);
		entity = repo.save(entity);
		log.info("saved record with key {}", entity.getId().toString());
		return mapper.toDomain(entity);
	}

	@Override
	public Optional<Notification> findById(UUID id) {
		return repo.findById(id)
			.map(mapper::toDomain);
	}

	@Override
	public void makeNotificationSeen(UUID id) {
		NotificationEntity entity = repo.findById(id)
			.orElseThrow(() -> new ValidationException("notification.not.found", Map.of("id", id)));

		if(entity.getSeen() == true) {
			throw new ValidationException("notification.already.seen", Map.of("id", id));
		} else {
			entity.setSeen(true);
			repo.save(entity);
			log.info("marked notification with id {} as seen", id);
		}
	}

	@Override
	public void deleteById(UUID id) {
		if(!repo.existsById(id)) {
			throw new ValidationException("notification.not.found", Map.of("id", id));
		}
		repo.deleteById(id);
		log.info("deleted notification with id {}", id);	
	}

	@Override
	public Boolean compensate(NotificationId id, SagaOutcome sagaState) {
		if(sagaState != SagaOutcome.COMPENSATE) {
			throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
		}
		var found = findById(id.value());
		if (found.isEmpty()) {
			return false;
		}
		return repo.delete(id.value()) == 1;
	}
}
