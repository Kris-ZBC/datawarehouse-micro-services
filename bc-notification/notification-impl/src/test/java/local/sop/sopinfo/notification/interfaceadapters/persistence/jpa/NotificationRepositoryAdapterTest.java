package local.sop.sopinfo.notification.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.sopinfo.notification.domain.model.Notification;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

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





}
