package local.sop.datawarehouse.notification.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.notification.application.api.NotificationDirectory;
import local.sop.datawarehouse.notification.application.api.dto.CreateNotificationCmd;
import local.sop.datawarehouse.notification.application.api.dto.NotificationResponse;
import local.sop.datawarehouse.notification.domain.model.Notification;
import local.sop.datawarehouse.notification.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.notification.domain.model.valueobjects.MessageRef;
import local.sop.datawarehouse.notification.domain.model.valueobjects.NotificationId;
import local.sop.datawarehouse.notification.domain.ports.out.NotificationRepositoryPort;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=none",
	"bc.qualifier=notification"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationApplicationServiceTest {
	private ReloadableResourceBundleMessageSource ms;
	private final Locale DA = Locale.forLanguageTag("da");

	@BeforeEach
	public void setup() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
				ms.setBasenames("classpath:i18n/common-web-messages", "classpath:i18n/common-security-messages", 
            "classpath:i18n/common-data-messages", "classpath:i18n/common-core-messages", "classpath:i18n/notification-messages");
		ms.setDefaultEncoding("UTF-8");
		ms.setFallbackToSystemLocale(false);
		ms.setUseCodeAsDefaultMessage(false);
		ms.setCacheMillis(0);
		this.ms = ms;
	}

	@Autowired
	private NotificationDirectory directory;

	@MockitoBean
	private NotificationRepositoryPort repo;

	@Test
	void shouldCreateNotification_whenValidObject() {
		UUID messageRef = UUID.randomUUID();
		Notification notification = Notification.builder()
			.messageRef(new MessageRef(messageRef))
			.build();
		when(repo.save(Mockito.any())).thenReturn(notification);

		UUID id = directory.createNotification(new CreateNotificationCmd(messageRef));
		assertNotNull(id);
		assertEquals(notification.getId().value(), id);

		Mockito.verify(repo, Mockito.times(1)).save(Mockito.any());
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldFindNotificationById_whenValidId() {
		Notification notification = Notification.builder()
			.messageRef(new MessageRef(UUID.randomUUID()))
			.build();

		when(repo.findById(Mockito.any())).thenReturn(Optional.of(notification));

		Optional<NotificationResponse> response = directory.findById(notification.getId().value());

		assertTrue(response.isPresent());
		assertEquals(notification.getId().value(), response.get().id());
	
		Mockito.verify(repo, Mockito.times(1)).findById(Mockito.any());
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldMakeNotificationSeen_whenValidId() {
		UUID id = UUID.randomUUID();

		directory.makeNotificationSeen(id);

		Mockito.verify(repo, Mockito.times(1)).makeNotificationSeen(id);
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldDeleteNotificationById_whenValidId() {
		UUID id = UUID.randomUUID();

		directory.deleteNotification(id);

		Mockito.verify(repo, Mockito.times(1)).deleteById(id);
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldThrowException_whenCreateNotificationFails() {
		ValidationException ex = assertThrows(ValidationException.class, () -> directory.createNotification(new CreateNotificationCmd(null)));

		assertEquals("Key er krævet", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

		Mockito.verify(repo, Mockito.times(0)).save(Mockito.any());
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldThrowException_whenFindByIdFails() {
		UUID id = UUID.randomUUID();

		Mockito.doThrow(new ValidationException("notification.not.found", Map.of("id", id))).when(repo).findById(Mockito.any());

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.findById(id));
		assertEquals("Notifikation blev ikke fundet", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

		Mockito.verify(repo, Mockito.times(1)).findById(id);
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldThrowException_whenMakeNotificationSeenFails() {
		UUID id = UUID.randomUUID();

		Mockito.doThrow(new ValidationException("notification.not.found", Map.of("id", id))).when(repo).makeNotificationSeen(Mockito.any());

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.makeNotificationSeen(id));
		assertEquals("Notifikation blev ikke fundet", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

		Mockito.verify(repo, Mockito.times(1)).makeNotificationSeen(id);
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldThrowException_whenDeleteNotificationFails() {
		UUID id = UUID.randomUUID();

		Mockito.doThrow(new ValidationException("notification.not.found", Map.of("id", id))).when(repo).deleteById(Mockito.any());

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.deleteNotification(id));
		assertEquals("Notifikation blev ikke fundet", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

		Mockito.verify(repo, Mockito.times(1)).deleteById(id);
		Mockito.verifyNoMoreInteractions(repo);
	}

	@Test
	void shouldThrowException_whenCreateNotificationFailsUnexpectedly() {
		when(repo.save(Mockito.any())).thenThrow(new RuntimeException("db crash"));

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.createNotification(new CreateNotificationCmd(UUID.randomUUID())));
		assertEquals("Oprettelse af notifikation mislykkedes", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));
	}

	@Test
	void shouldThrowException_whenFindByIdFailsUnexpectedly() {
		when(repo.findById(Mockito.any())).thenThrow(new RuntimeException("db crash"));

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.findById(UUID.randomUUID()));
		assertEquals("Søgning efter notifikation via id mislykkedes", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));
	}

	@Test
	void shouldThrowException_whenMakeNotificationSeenFailsUnexpectedly() {
		Mockito.doThrow(new RuntimeException("db crash")).when(repo).makeNotificationSeen(Mockito.any());

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.makeNotificationSeen(UUID.randomUUID()));
		assertEquals("Markering af notifikation som set mislykkedes", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));
	}

	@Test
	void shouldThrowException_whenDeleteNotificationFailsUnexpectedly() {
		Mockito.doThrow(new RuntimeException("db crash")).when(repo).deleteById(Mockito.any());

		ValidationException ex = assertThrows(ValidationException.class, () -> directory.deleteNotification(UUID.randomUUID()));
		assertEquals("Sletning af notifikation mislykkedes", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));
	}

	@Test
	void compensate_whenNotificationNotFound_shouldReturnIdempotentFalse() {
		UUID id = UUID.randomUUID();

		when(repo.findById(id)).thenReturn(Optional.empty());

		ResponseCompensated result = directory.compensate(id, getClass(),SagaOutcome.COMPENSATE);

		assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
		assertFalse(result.success());
		verify(repo).findById(id);
		verify(repo, never()).compensate(any(), any());
	}

	@Test
	void compensate_whenNotificationExistsAndCompensateSucceeds_shouldReturnCompensateTrue() {
		UUID id = UUID.randomUUID();
		UUID messageRef = UUID.randomUUID();

		Notification notification = Notification.builder()
    		.id(NotificationId.of(id))
    		.messageRef(new MessageRef(messageRef))
    		.seen(false)
    		.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
    		.build();

		when(repo.findById(id)).thenReturn(Optional.of(notification));
		when(repo.compensate(NotificationId.of(id), SagaOutcome.COMPENSATE)).thenReturn(true);

		ResponseCompensated result = directory.compensate(id, getClass(), SagaOutcome.COMPENSATE);

		assertEquals(SagaOutcome.COMPENSATE, result.sagaState());
		assertTrue(result.success());
		verify(repo).findById(id);
		verify(repo).compensate(NotificationId.of(id), SagaOutcome.COMPENSATE);
	}

	@Test
	void compensate_whenNotificationExistsButCompensateReturnsFalse_shouldReturnIdempodentFalse() {
		UUID id = UUID.randomUUID();
		UUID messageRef = UUID.randomUUID();

		Notification notification = Notification.builder()
			.id(NotificationId.of(id))
    		.messageRef(new MessageRef(messageRef))
    		.seen(false)
    		.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
    		.build();

		when(repo.findById(id)).thenReturn(Optional.of(notification));
		when(repo.compensate(NotificationId.of(id), SagaOutcome.COMPENSATE)).thenReturn(false);

		ResponseCompensated result = directory.compensate(id, getClass(), SagaOutcome.COMPENSATE);

		assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
		assertFalse(result.success());
		verify(repo).findById(id);
		verify(repo).compensate(NotificationId.of(id), SagaOutcome.COMPENSATE);
	}
}
