package local.sop.sopinfo.sopeducation.application.service;

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
import local.sop.sopinfo.sopeducation.application.api.SopEducationDirectory;
import local.sop.sopinfo.sopeducation.application.api.dto.CreateSopEducationCmd;
import local.sop.sopinfo.sopeducation.application.api.dto.ToggleActivateSopEducationCmd;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;
import local.sop.sopinfo.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sopeducation.domain.ports.out.SopEducationPort;
import local.sop.sopinfo.sopeducation.domain.service.SopEducationDomain;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SopEducationApplicationServiceAspectTest.TestConfig.class)
class SopEducationApplicationServiceAspectTest {

    @Autowired
    private SopEducationDirectory service;

    

    @Autowired
    private ApplicationContext context;

    private static final UUID SOP_REF       = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF  = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, EDUCATION_REF);

    @BeforeEach
    void resetMocks() {
        reset(
            context.getBean("sopPort", CompositeKeyValidator.class),
            context.getBean("educationPort", CompositeKeyValidator.class),
            context.getBean(SopEducationPort.class),
            context.getBean(SopEducationDomain.class)
        );
    }

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import({SopEducationApplicationService.class})
    static class TestConfig {

        @Bean("sopPort")
        CompositeKeyValidator sopPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean("educationPort")
        CompositeKeyValidator educationPort() {
            return mock(CompositeKeyValidator.class);
        }

        @Bean
        CompositeKeyValidationAspect compositeKeyValidationAspect(ApplicationContext context) {
            return new CompositeKeyValidationAspect(context);
        }

        @Bean
        SopEducationPort sopEducationPort() {
            return mock(SopEducationPort.class);
        }

        @Bean
        SopEducationDomain sopEducationDomain() {
            return mock(SopEducationDomain.class);
        }
    }

    // ── Aspect intercepts create ───────────────────────────────────────────────

    @Test
    void create_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);
        SopEducationPort repository         = context.getBean(SopEducationPort.class);
        SopEducationDomain domain           = context.getBean(SopEducationDomain.class);

         SopEducation saved = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

         when(domain.createSopEducation(VALID_KEY, true)).thenReturn(saved);

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(educationPort.exists(EDUCATION_REF)).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.create(new CreateSopEducationCmd(VALID_KEY, true)));

        verify(sopPort).exists(SOP_REF);
        verify(educationPort).exists(EDUCATION_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(false);
        when(educationPort.exists(EDUCATION_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateSopEducationCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(educationPort, never()).exists(any());
    }

    @Test
    void create_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(educationPort.exists(EDUCATION_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateSopEducationCmd(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(educationPort).exists(EDUCATION_REF);
    }

    @Test
    void create_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(new CreateSopEducationCmd(null, true)));

        assertEquals("key.required", ex.getMessage());
    }

    // ── Aspect intercepts toggleActive ─────────────────────────────────────────

    @Test
    void toggleActive_shouldBeIntercepted_whenBothKeysExist() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);
        SopEducationPort repository         = context.getBean(SopEducationPort.class);
        SopEducationDomain domain           = context.getBean(SopEducationDomain.class);

        SopEducation existing = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(educationPort.exists(EDUCATION_REF)).thenReturn(true);
        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateSopEducation(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        assertDoesNotThrow(() -> service.toggleActive(new ToggleActivateSopEducationCmd(VALID_KEY, false)));

        verify(sopPort).exists(SOP_REF);
        verify(educationPort).exists(EDUCATION_REF);
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(false);
        when(educationPort.exists(EDUCATION_REF)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivateSopEducationCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(educationPort, never()).exists(any());
    }

    @Test
    void toggleActive_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);

        when(sopPort.exists(SOP_REF)).thenReturn(true);
        when(educationPort.exists(EDUCATION_REF)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.toggleActive(new ToggleActivateSopEducationCmd(VALID_KEY, false)));

        assertEquals("key.invalid", ex.getMessage());
        verify(sopPort).exists(SOP_REF);
        verify(educationPort).exists(EDUCATION_REF);
    }

    // ── Aspect does NOT intercept read-only methods ────────────────────────────

    @Test
    void findById_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);
        SopEducationPort repository         = context.getBean(SopEducationPort.class);

        SopEducation existing = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        service.findById(VALID_KEY);

        verify(sopPort, never()).exists(any());
        verify(educationPort, never()).exists(any());
    }

    @Test
    void getBySopRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);
        SopEducationPort repository         = context.getBean(SopEducationPort.class);

        when(repository.findBySopRef(SOP_REF)).thenReturn(List.of());

        service.getBySopRef(SOP_REF);

        verify(sopPort, never()).exists(any());
        verify(educationPort, never()).exists(any());
    }

    @Test
    void getByEducationRef_shouldNotBeIntercepted_byAspect() {
        CompositeKeyValidator sopPort       = context.getBean("sopPort", CompositeKeyValidator.class);
        CompositeKeyValidator educationPort = context.getBean("educationPort", CompositeKeyValidator.class);
        SopEducationPort repository         = context.getBean(SopEducationPort.class);

        when(repository.findByEducationRef(EDUCATION_REF)).thenReturn(List.of());

        service.getByEducationRef(EDUCATION_REF);

        verify(sopPort, never()).exists(any());
        verify(educationPort, never()).exists(any());
    }
}
