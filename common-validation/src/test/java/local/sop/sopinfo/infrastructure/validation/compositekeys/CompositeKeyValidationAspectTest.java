package local.sop.sopinfo.infrastructure.validation.compositekeys;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@SpringBootTest(classes = CompositeKeyValidationTestApp.class, properties = "compositekey.validate.enabled=true")
class CompositeKeyValidationAspectTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TestService testService;

    private static final UUID KEY1 = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID KEY2 = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");

    // CompositeKey now takes varargs keys()
    private static final CompositeKey VALID_KEY = new CompositeKey(KEY1, KEY2);

    @BeforeEach
    void resetMocks() {
        reset(
            context.getBean("key1Port", CompositeKeyValidator.class),
            context.getBean("key2Port", CompositeKeyValidator.class)
        );
    }

    // ── Aspect registration ────────────────────────────────────────────────────

    @Test
    void aspect_shouldBeRegistered() {
        assertNotNull(context.getBean(CompositeKeyValidationAspect.class));
    }

    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    void validateKey_shouldFail_whenNoMockSetup() {
        // Mock returns false by default — aspect fires and throws
        assertThrows(ValidationException.class,
                () -> testService.execute(new TestCommand(VALID_KEY, true)));
    }

    @Test
    void validateKey_shouldPass_whenBothKeysExist() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(true);
        when(key2Port.exists(KEY2)).thenReturn(true);

        assertDoesNotThrow(() -> testService.execute(new TestCommand(VALID_KEY, true)));
    }

    @Test
    void validateKey_shouldInvokeCorrectValidators_whenBothKeysExist() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(true);
        when(key2Port.exists(KEY2)).thenReturn(true);

        testService.execute(new TestCommand(VALID_KEY, true));

        // Verify correct port called with correct key — not crossed
        verify(key1Port).exists(KEY1);
        verify(key2Port).exists(KEY2);
    }

    @Test
    void validateKey_shouldStillWork_withDirectCompositeKey() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(true);
        when(key2Port.exists(KEY2)).thenReturn(true);

        assertDoesNotThrow(() -> testService.executeWithDirectKey(VALID_KEY));
    }

    // ── Unhappy path — key does not exist ──────────────────────────────────────

    @Test
    void validateKey_shouldThrowValidationException_whenKey1DoesNotExist() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(false);
        when(key2Port.exists(KEY2)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> testService.execute(new TestCommand(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
    }

    @Test
    void validateKey_shouldThrowValidationException_whenKey2DoesNotExist() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(true);
        when(key2Port.exists(KEY2)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> testService.execute(new TestCommand(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
    }

    @Test
    void validateKey_shouldThrowValidationException_whenBothKeysDoNotExist() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(false);
        when(key2Port.exists(KEY2)).thenReturn(false);

        // Fail-fast — key1 fails, key2 never checked
        ValidationException ex = assertThrows(ValidationException.class,
                () -> testService.execute(new TestCommand(VALID_KEY, true)));

        assertEquals("key.invalid", ex.getMessage());
    }

    // ── Unhappy path — missing or null id ─────────────────────────────────────

    @Test
    void validateKey_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> testService.execute(new TestCommand(null, true)));

        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void validateKey_shouldThrowValidationException_whenCommandHasNoCompositeKey() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> testService.executeWithNoId(new TestCommandNoId("test")));

        assertEquals("key.required", ex.getMessage());
    }

    // ── Port/key count mismatch ────────────────────────────────────────────────

    @Test
    void validateKey_shouldThrowValidationException_whenPortCountDoesNotMatchKeyCount() {
        CompositeKeyValidator key1Port = context.getBean("key1Port", CompositeKeyValidator.class);
        CompositeKeyValidator key2Port = context.getBean("key2Port", CompositeKeyValidator.class);

        when(key1Port.exists(KEY1)).thenReturn(true);
        when(key2Port.exists(KEY2)).thenReturn(true);

        // THREE keys but only TWO ports declared in @ValidateCompositeKey
        UUID KEY3 = UUID.fromString("333e4567-e89b-12d3-a456-426614174333");
        CompositeKey threeKeyComposite = new CompositeKey(KEY1, KEY2, KEY3);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> testService.executeWithDirectKey(threeKeyComposite));

        assertEquals("key.portmismatch", ex.getMessage());
    }
}