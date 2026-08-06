package local.sop.datawarehouse.messageperson.service;

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
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.messageperson.application.api.MessagePersonDirectory;
import local.sop.datawarehouse.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.datawarehouse.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;
import local.sop.datawarehouse.messageperson.application.service.MessagePersonApplicationService;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;
import local.sop.datawarehouse.messageperson.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.messageperson.domain.ports.out.MessagePersonPort;
import local.sop.datawarehouse.messageperson.domain.service.MessagePersonDomain;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MessagePersonApplicationServiceAspectTest.TestConfig.class})
class MessagePersonApplicationServiceAspectTest {

    @Autowired
    private MessagePersonDirectory service;

    @Autowired
    private ApplicationContext context;

    private static final UUID MESSAGE_REF = UUID.randomUUID();
    private static final UUID PERSON_REF  = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(MESSAGE_REF, PERSON_REF);

    @BeforeEach
    void resetMocks() {
        reset(
            context.getBean("messagePort", CompositeKeyValidator.class),
            context.getBean("personPort", CompositeKeyValidator.class),
            context.getBean(MessagePersonPort.class),
            context.getBean(MessagePersonDomain.class)
        );
    }

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import(MessagePersonApplicationService.class)
    static class TestConfig {

        @Bean("messagePort")
        CompositeKeyValidator messagePort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean("personPort")
        CompositeKeyValidator personPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean
        CompositeKeyValidationAspect compositeKeyValidationAspect(ApplicationContext context) {
            return new CompositeKeyValidationAspect(context);
        }

        @Bean
        MessagePersonPort messagePersonPort() {
            return mock(MessagePersonPort.class);
        }

        @Bean
        MessagePersonDomain messagePersonDomain() {
            return mock(MessagePersonDomain.class);
        }
    
    }

    // ── Aspect intercepts create ───────────────────────────────────────────────

    @Test
    void create_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        MessagePersonPort repository = context.getBean(MessagePersonPort.class);
        MessagePersonDomain domain = context.getBean(MessagePersonDomain.class);

        MessagePerson saved = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(domain.createMessagePerson(VALID_KEY, true)).thenReturn(saved);

        when(messagePort.exists(MESSAGE_REF)).thenReturn(true);
        when(personPort.exists(PERSON_REF)).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.create(new CreateMessagePersonCmd(VALID_KEY, true)));

        verify(messagePort).exists(MESSAGE_REF);
        verify(personPort).exists(PERSON_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);

        when(messagePort.exists(MESSAGE_REF)).thenReturn(false);
        when(personPort.exists(PERSON_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateMessagePersonCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(messagePort).exists(MESSAGE_REF);
        verify(personPort, never()).exists(any());
    }

    @Test
    void create_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);

        when(messagePort.exists(MESSAGE_REF)).thenReturn(true);
        when(personPort.exists(PERSON_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateMessagePersonCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(messagePort).exists(MESSAGE_REF);
        verify(personPort).exists(PERSON_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateMessagePersonCmd(null, true)));

        assertEquals("key.required", ex.getMessage());
    }

    // ── Aspect intercepts toggleActive ─────────────────────────────────────────

    @Test
    void toggleActive_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        MessagePersonPort repository = context.getBean(MessagePersonPort.class);
        MessagePersonDomain domain = context.getBean(MessagePersonDomain.class);

        MessagePerson existing = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(messagePort.exists(MESSAGE_REF)).thenReturn(true);
        when(personPort.exists(PERSON_REF)).thenReturn(true);
        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateMessagePerson(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        assertDoesNotThrow(() -> service.toggleActive(new ToggleActivateMessagePersonCmd(VALID_KEY, false)));

        verify(messagePort).exists(MESSAGE_REF);
        verify(personPort).exists(PERSON_REF);
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);

        when(messagePort.exists(MESSAGE_REF)).thenReturn(false);
        when(personPort.exists(PERSON_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivateMessagePersonCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(messagePort).exists(MESSAGE_REF);
        verify(personPort, never()).exists(any());
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);

        when(messagePort.exists(MESSAGE_REF)).thenReturn(true);
        when(personPort.exists(PERSON_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivateMessagePersonCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(messagePort).exists(MESSAGE_REF);
        verify(personPort).exists(PERSON_REF);
    }

    // ── Aspect does NOT intercept read-only methods ────────────────────────────

    @Test
    void findById_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        MessagePersonPort repository = context.getBean(MessagePersonPort.class);

        MessagePerson existing = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        service.findById(VALID_KEY);

        verify(messagePort, never()).exists(any());
        verify(personPort, never()).exists(any());
    }

    @Test
    void getByMessageRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        MessagePersonPort repository = context.getBean(MessagePersonPort.class);

        when(repository.findByMessageRef(MESSAGE_REF)).thenReturn(List.of());

        service.getByMessageRef(MESSAGE_REF);

        verify(messagePort, never()).exists(any());
        verify(personPort, never()).exists(any());
    }

    @Test
    void getByPersonRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator messagePort = context.getBean("messagePort", CompositeKeyValidator.class);
        CompositeKeyValidator personPort = context.getBean("personPort", CompositeKeyValidator.class);
        MessagePersonPort repository = context.getBean(MessagePersonPort.class);

        when(repository.findByPersonRef(PERSON_REF)).thenReturn(List.of());

        service.getByPersonRef(PERSON_REF);

        verify(messagePort, never()).exists(any());
        verify(personPort, never()).exists(any());
    }

}
