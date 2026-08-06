package local.sop.sopinfo.personnotification.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.personnotification.domain.model.PersonNotification;
import local.sop.datawarehouse.personnotification.domain.model.valueobjects.CreatedAtTimestamp;

public class PersonNotificationTest {

    private static final UUID NOTIFICATION_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final CompositeKey VALID_ID    = new CompositeKey(NOTIFICATION_REF, PERSON_REF);
    private static final CreatedAtTimestamp CREATED_AT = new CreatedAtTimestamp(LocalDateTime.now().minusDays(1));
    private static final CreatedAtTimestamp NEW_CREATED_AT = new CreatedAtTimestamp(LocalDateTime.now().minusDays(2));

    // ── Builder happy path ─────────────────────────────────────────────────────

    @Test
    void builder_shouldCreateAggregate_whenAllFieldsAreValid() {
        PersonNotification aggregate = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(VALID_ID, aggregate.getId());
        assertTrue(aggregate.isActive());
        assertEquals(CREATED_AT, aggregate.getCreatedAt());
    }

    @Test
    void builder_shouldCreateAggregate_whenActiveIsFalse() {
        PersonNotification aggregate = PersonNotification.builder()
                .id(VALID_ID)
                .active(false)
                .createdAt(CREATED_AT)
                .build();

        assertFalse(aggregate.isActive());
    }

    @Test
    void builder_shouldCreateAggregate_whenCreatedAtIsNull() {
        // createdAt is validated in its own value object — builder allows null
        PersonNotification aggregate = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(null)
                .build();

        assertNull(aggregate.getCreatedAt());
    }

    @Test
    void builder_shouldCreateAggregate_whenIdIsNull() {
        // id is validated in its own value object — builder allows null
        PersonNotification aggregate = PersonNotification.builder()
                .id(null)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertNull(aggregate.getId());
    }

    // ── Builder unhappy path ───────────────────────────────────────────────────

    @Test
    void builder_shouldThrowValidationException_whenActiveIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                PersonNotification.builder()
                        .id(VALID_ID)
                        .createdAt(CREATED_AT)
                        .build());

        assertEquals("personnotification.active.required", ex.getMessage());
    }

    // ── withActive ─────────────────────────────────────────────────────────────

    @Test
    void withActive_shouldReturnNewInstance_withUpdatedActive() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        PersonNotification updated = original.withActive(false);

        assertFalse(updated.isActive());
    }

    @Test
    void withActive_shouldReturnNewInstance_notSameReference() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        PersonNotification updated = original.withActive(false);

        assertNotSame(original, updated);
    }

    @Test
    void withActive_shouldPreserveId_andCreatedAt() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        PersonNotification updated = original.withActive(false);

        assertEquals(VALID_ID, updated.getId());
        assertEquals(CREATED_AT, updated.getCreatedAt());
    }

    // ── withCreatedAt ──────────────────────────────────────────────────────────

    @Test
    void withCreatedAt_shouldReturnNewInstance_withUpdatedCreatedAt() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        PersonNotification updated = original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(NEW_CREATED_AT, updated.getCreatedAt());
    }

    @Test
    void withCreatedAt_shouldReturnNewInstance_notSameReference() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        PersonNotification updated = original.withCreatedAt(NEW_CREATED_AT);

        assertNotSame(original, updated);
    }

    @Test
    void withCreatedAt_shouldPreserveId_andActive() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        PersonNotification updated = original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(VALID_ID, updated.getId());
        assertTrue(updated.isActive());
    }

    // ── Immutability ───────────────────────────────────────────────────────────

    @Test
    void withActive_shouldNotMutateOriginal() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        original.withActive(false);

        assertTrue(original.isActive());
    }

    @Test
    void withCreatedAt_shouldNotMutateOriginal() {
        PersonNotification original = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

         original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(CREATED_AT, original.getCreatedAt());
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    @Test
    void getId_shouldReturnCorrectValue() {
        PersonNotification aggregate = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(VALID_ID, aggregate.getId());
    }

    @Test
    void isActive_shouldReturnCorrectValue() {
        PersonNotification aggregate = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertTrue(aggregate.isActive());
    }

    @Test
    void getCreatedAt_shouldReturnCorrectValue() {
        PersonNotification aggregate = PersonNotification.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(CREATED_AT, aggregate.getCreatedAt());
    }

}
