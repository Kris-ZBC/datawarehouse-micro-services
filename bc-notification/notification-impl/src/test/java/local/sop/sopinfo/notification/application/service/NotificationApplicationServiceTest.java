package local.sop.sopinfo.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.Optional;
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

import local.sop.sopinfo.notification.application.api.NotificationDirectory;
import local.sop.sopinfo.notification.application.api.dto.CreateNotificationCmd;
import local.sop.sopinfo.notification.application.api.dto.NotificationResponse;
import local.sop.sopinfo.notification.domain.model.Notification;
import local.sop.sopinfo.notification.domain.model.valueobjects.MessageRef;
import local.sop.sopinfo.notification.domain.ports.out.NotificationRepositoryPort;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

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
}
