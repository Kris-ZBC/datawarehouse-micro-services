package local.sop.sopinfo.personnotification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import local.sop.common.libs.infrastructure.validation.compositekeys.CompositeKeyValidationAspect;

import local.sop.sopinfo.personnotification.application.api.PersonNotificationDirectory;
import local.sop.sopinfo.personnotification.application.api.dto.CreatePersonNotificationCmd;
import local.sop.sopinfo.personnotification.application.api.dto.ToggleActivatePersonNotificationCmd;
import local.sop.sopinfo.personnotification.application.service.PersonNotificationApplicationService;
import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.personnotification.domain.ports.out.PersonNotificationPort;
import local.sop.sopinfo.personnotification.domain.service.PersonNotificationDomain;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PersonNotificationApplicationServiceAspectTest.TestConfig.class})
class PersonNotificationApplicationServiceAspectTest {

    @Autowired
    private PersonNotificationDirectory service;

    @Autowired
    private ApplicationContext context;

    private static final UUID NOTIFICATION_REF = UUID.randomUUID();
    private static final UUID PERSON_REF  = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(PERSON_REF, NOTIFICATION_REF);

    @BeforeEach
    void resetMocks() {
        reset(
            context.getBean("personPort", CompositeKeyValidator.class),
            context.getBean("notificationPort", CompositeKeyValidator.class),
            context.getBean(PersonNotificationPort.class),
            context.getBean(PersonNotificationDomain.class)
        );
    }

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import(PersonNotificationApplicationService.class)
    static class TestConfig {

        @Bean("personPort")
        CompositeKeyValidator personPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean("notificationPort")
        CompositeKeyValidator notificationPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean
        CompositeKeyValidationAspect compositeKeyValidationAspect(ApplicationContext context) {
            return new CompositeKeyValidationAspect(context);
        }

        @Bean
        PersonNotificationPort personNotificationPort() {
            return mock(PersonNotificationPort.class);
        }

        @Bean
        PersonNotificationDomain personNotificationDomain() {
            return mock(PersonNotificationDomain.class);
        }
    
    }

    // ── Aspect intercepts create ───────────────────────────────────────────────

    @Test
    void create_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        PersonNotificationPort repository = context.getBean(PersonNotificationPort.class);
        PersonNotificationDomain domain = context.getBean(PersonNotificationDomain.class);

        PersonNotification saved = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(domain.createPersonNotification(VALID_KEY, true)).thenReturn(saved);

        when(notificationPort.exists(NOTIFICATION_REF)).thenReturn(true);
        when(personPort.exists(PERSON_REF)).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.create(new CreatePersonNotificationCmd(VALID_KEY, true)));

        verify(notificationPort).exists(NOTIFICATION_REF);
        verify(personPort).exists(PERSON_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);

        when(personPort.exists(PERSON_REF)).thenReturn(false);
        when(notificationPort.exists(NOTIFICATION_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreatePersonNotificationCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(personPort).exists(PERSON_REF);
        verify(notificationPort, never()).exists(any());
    }

    @Test
    void create_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);

        when(personPort.exists(PERSON_REF)).thenReturn(true);
        when(notificationPort.exists(NOTIFICATION_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreatePersonNotificationCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(personPort).exists(PERSON_REF);
        verify(notificationPort).exists(NOTIFICATION_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreatePersonNotificationCmd(null, true)));

        assertEquals("key.required", ex.getMessage());
    }

    // ── Aspect intercepts toggleActive ─────────────────────────────────────────

    @Test
    void toggleActive_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        PersonNotificationPort repository = context.getBean(PersonNotificationPort.class);
        PersonNotificationDomain domain = context.getBean(PersonNotificationDomain.class);

        PersonNotification existing = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(notificationPort.exists(NOTIFICATION_REF)).thenReturn(true);
        when(personPort.exists(PERSON_REF)).thenReturn(true);
        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivatePersonNotification(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        assertDoesNotThrow(() -> service.toggleActive(new ToggleActivatePersonNotificationCmd(VALID_KEY, false)));

        verify(notificationPort).exists(NOTIFICATION_REF);
        verify(personPort).exists(PERSON_REF);
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);
        
        when(personPort.exists(PERSON_REF)).thenReturn(false);
        when(notificationPort.exists(NOTIFICATION_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivatePersonNotificationCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(personPort).exists(PERSON_REF);
        verify(notificationPort, never()).exists(any());
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);

        when(personPort.exists(PERSON_REF)).thenReturn(true);
        when(notificationPort.exists(NOTIFICATION_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivatePersonNotificationCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(personPort).exists(PERSON_REF);
        verify(notificationPort).exists(NOTIFICATION_REF);
    }

    // ── Aspect does NOT intercept read-only methods ────────────────────────────

    @Test
    void findById_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        PersonNotificationPort repository = context.getBean(PersonNotificationPort.class);

        PersonNotification existing = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        service.findById(VALID_KEY);

        verify(notificationPort, never()).exists(any());
        verify(personPort, never()).exists(any());
    }

    @Test
    void getByNotificationRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        PersonNotificationPort repository = context.getBean(PersonNotificationPort.class);

        when(repository.findByNotificationRef(NOTIFICATION_REF)).thenReturn(List.of());

        service.getByNotificationRef(NOTIFICATION_REF);

        verify(notificationPort, never()).exists(any());
        verify(personPort, never()).exists(any());
    }

    @Test
    void getByPersonRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator notificationPort = context.getBean("notificationPort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        PersonNotificationPort repository = context.getBean(PersonNotificationPort.class);

        when(repository.findByPersonRef(PERSON_REF)).thenReturn(List.of());

        service.getByPersonRef(PERSON_REF);

        verify(notificationPort, never()).exists(any());
        verify(personPort, never()).exists(any());
    }

}
