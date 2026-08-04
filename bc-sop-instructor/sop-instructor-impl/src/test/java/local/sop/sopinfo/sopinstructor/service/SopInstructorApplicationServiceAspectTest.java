package local.sop.sopinfo.sopinstructor.service;

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
import local.sop.sopinfo.sopinstructor.application.api.SopInstructorDirectory;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.api.dto.ToggleActivateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.service.SopInstructorApplicationService;
import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sopinstructor.domain.ports.out.SopInstructorPort;
import local.sop.sopinfo.sopinstructor.domain.service.SopInstructorDomain;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SopInstructorApplicationServiceAspectTest.TestConfig.class})
class SopInstructorApplicationServiceAspectTest {

    @Autowired
    private SopInstructorDirectory service;

    @Autowired
    private ApplicationContext context;

    private static final UUID SOP_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF  = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, INSTRUCTOR_REF);

    @BeforeEach
    void resetMocks() {
        reset(
            context.getBean("sopPort", CompositeKeyValidator.class),
            context.getBean("instructorPort", CompositeKeyValidator.class),
            context.getBean(SopInstructorPort.class),
            context.getBean(SopInstructorDomain.class)
        );
    }

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import(SopInstructorApplicationService.class)
    static class TestConfig {

        @Bean("sopPort")
        CompositeKeyValidator sopPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean("instructorPort")
        CompositeKeyValidator instructorPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean
        CompositeKeyValidationAspect compositeKeyValidationAspect(ApplicationContext context) {
            return new CompositeKeyValidationAspect(context);
        }

        @Bean
        SopInstructorPort sopInstructorPort() {
            return mock(SopInstructorPort.class);
        }

        @Bean
        SopInstructorDomain sopInstructorDomain() {
            return mock(SopInstructorDomain.class);
        }
    
    }

    // ── Aspect intercepts create ───────────────────────────────────────────────

    @Test
    void create_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);
        SopInstructorPort repository = context.getBean(SopInstructorPort.class);
        SopInstructorDomain domain = context.getBean(SopInstructorDomain.class);

        SopInstructor saved = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(domain.createSopInstructor(VALID_KEY, true)).thenReturn(saved);

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(instructorPort.exists(INSTRUCTOR_REF)).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.create(new CreateSopInstructorCmd(VALID_KEY, true)));

        verify(sopPort).exists(SOP_REF);
        verify(instructorPort).exists(INSTRUCTOR_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(false);
        when(instructorPort.exists(INSTRUCTOR_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateSopInstructorCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(instructorPort, never()).exists(any());
    }

    @Test
    void create_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(instructorPort.exists(INSTRUCTOR_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateSopInstructorCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(instructorPort).exists(INSTRUCTOR_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateSopInstructorCmd(null, true)));

        assertEquals("key.required", ex.getMessage());
    }

    // ── Aspect intercepts toggleActive ─────────────────────────────────────────

    @Test
    void toggleActive_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);
        SopInstructorPort repository = context.getBean(SopInstructorPort.class);
        SopInstructorDomain domain = context.getBean(SopInstructorDomain.class);

        SopInstructor existing = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(instructorPort.exists(INSTRUCTOR_REF)).thenReturn(true);
        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateSopInstructor(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        assertDoesNotThrow(() -> service.toggleActive(new ToggleActivateSopInstructorCmd(VALID_KEY, false)));

        verify(sopPort).exists(SOP_REF);
        verify(instructorPort).exists(INSTRUCTOR_REF);
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(false);
        when(instructorPort.exists(INSTRUCTOR_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivateSopInstructorCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(instructorPort, never()).exists(any());
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(instructorPort.exists(INSTRUCTOR_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivateSopInstructorCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(instructorPort).exists(INSTRUCTOR_REF);
    }

    // ── Aspect does NOT intercept read-only methods ────────────────────────────

    @Test
    void findById_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);
        SopInstructorPort repository = context.getBean(SopInstructorPort.class);

        SopInstructor existing = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        service.findById(VALID_KEY);

        verify(sopPort, never()).exists(any());
        verify(instructorPort, never()).exists(any());
    }

    @Test
    void getBySopRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);
        SopInstructorPort repository = context.getBean(SopInstructorPort.class);

        when(repository.findBySopRef(SOP_REF)).thenReturn(List.of());

        service.getBySopRef(SOP_REF);

        verify(sopPort, never()).exists(any());
        verify(instructorPort, never()).exists(any());
    }

    @Test
    void getByInstructorRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator sopPort = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator instructorPort = context.getBean("instructorPort", CompositeKeyValidator.class);
        SopInstructorPort repository = context.getBean(SopInstructorPort.class);

        when(repository.findByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of());

        service.getByInstructorRef(INSTRUCTOR_REF);

        verify(sopPort, never()).exists(any());
        verify(instructorPort, never()).exists(any());
    }

}
