package local.sop.datawarehouse.notification.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.notification.domain.model.Notification;
import local.sop.datawarehouse.notification.domain.model.valueobjects.NotificationId;

public class NotificationRepositoryAdapterTest {
	
	private NotificationMapper mapper;
	private NotificationSpringDataRepository repo;
	private NotificationRepositoryAdapter adapter;

	@BeforeEach
	void setup(){
		mapper = mock(NotificationMapper.class);
		repo = mock(NotificationSpringDataRepository.class);
		adapter = new NotificationRepositoryAdapter(mapper, repo);
	}

	@Test
	@DisplayName("should save notification")
	void shouldSaveNotification(){

		UUID id = UUID.randomUUID();

		Notification notification = mock(Notification.class);
		NotificationEntity entity = NotificationEntity.builder()
		 	.id(id)
			.build();

		when(mapper.toEntity(notification)).thenReturn(entity);
		when(repo.save(entity)).thenReturn(entity);
		when(mapper.toDomain(entity)).thenReturn(notification);

		Notification result = adapter.save(notification);

		assertNotNull(result);
		
		verify(mapper).toEntity(notification);
		verify(repo).save(entity);
		verify(mapper).toDomain(entity);
	}

	@Test 
	@DisplayName("should find notification by id")
	void shouldFindNotificationById(){

		UUID id = UUID.randomUUID();

		NotificationEntity entity = NotificationEntity.builder()
		    .id(id)
			.build();

		Notification notification = mock(Notification.class);

		when(repo.findById(id)).thenReturn(Optional.of(entity));
		when(mapper.toDomain(entity)).thenReturn(notification);


		Optional<Notification> result = adapter.findById(id);

		assertTrue(result.isPresent());

		verify(repo).findById(id);
		verify(mapper).toDomain(entity);
	}

	@Test 
	@DisplayName("should make notification as seen")
	void shouldMakeNotificationAsSeen(){

		UUID id = UUID.randomUUID();

		NotificationEntity entity = NotificationEntity.builder()
			.id(id)
			.build();

		entity.setSeen(false);


		when(repo.findById(id)).thenReturn(Optional.of(entity));
		adapter.makeNotificationSeen(id);
		assertTrue(entity.getSeen());
		verify(repo).save(entity);
	}

	@Test
	@DisplayName("should throw validation exception if notification is already seen")
	void shouldThrowValidationExceptionIfNotificationIsAlreadySeen(){

		UUID id = UUID.randomUUID();

		NotificationEntity entity = NotificationEntity.builder()
			.id(id)
			.build();

		entity.setSeen(true);

		when(repo.findById(id)).thenReturn(Optional.of(entity));

		assertThrows(
			ValidationException.class, 
			() -> adapter.makeNotificationSeen(id)
		);

		verify(repo).findById(id);
		verify(repo, never()).save(any());

	}


	@Test
	@DisplayName("should delete notification by id")
	void shouldDeleteNotificationById(){

		UUID id = UUID.randomUUID();

		when(repo.existsById(id)).thenReturn(true);
		adapter.deleteById(id);
		verify(repo).deleteById(id);
	}

	@Test
	@DisplayName("should throw validation exception if notification not found")
	void shouldThrowValidationExceptionIfNotificationNotFound(){

		UUID id = UUID.randomUUID();

		when(repo.existsById(id)).thenReturn(false);
		assertThrows(
			ValidationException.class, 
			() -> adapter.deleteById(id)
		);

	}

	@Test
	void compensate_wrongSagaState_shouldThrowConflictException() {
		NotificationId id = NotificationId.newId();

		assertThrows(ConflictException.class, () -> adapter.compensate(id, SagaOutcome.COMPENSATED));

		verifyNoInteractions(repo, mapper);
	}

	@Test
	void compensate_whenNotificationNotFound_shouldReturnFalse() {
		NotificationId id = NotificationId.newId();

		when(repo.findById(id.value())).thenReturn(Optional.empty());

		Boolean result = adapter.compensate(id, SagaOutcome.COMPENSATE);

		assertFalse(result);
		verify(repo).findById(id.value());
		verify(repo, never()).delete(any(UUID.class));
	}

	@Test
	void compensate_whenNotificationFoundAndDeleted_shouldReturnTrue() {
		NotificationId id = NotificationId.newId();
		NotificationEntity entity = new NotificationEntity();

		when(repo.findById(id.value())).thenReturn(Optional.of(entity));
		when(mapper.toDomain(entity)).thenReturn(mock(Notification.class));
		when(repo.delete(id.value())).thenReturn(1);

		Boolean result = adapter.compensate(id, SagaOutcome.COMPENSATE);

		assertTrue(result);
		verify(repo).findById(id.value());
		verify(repo).delete(id.value());
	}

	@Test
	void compensate_whenNotificationFoundButDeleteFails_shouldReturnFalse() {
		NotificationId id = NotificationId.newId();
		NotificationEntity entity = new NotificationEntity();

		when(repo.findById(id.value())).thenReturn(Optional.of(entity));
		when(mapper.toDomain(entity)).thenReturn(mock(Notification.class));
		when(repo.delete(id.value())).thenReturn(0);

		Boolean result = adapter.compensate(id, SagaOutcome.COMPENSATE);

		assertFalse(result);
		verify(repo).findById(id.value());
		verify(repo).delete(id.value());
	}
}
