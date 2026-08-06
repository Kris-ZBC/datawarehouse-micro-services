package local.sop.datawarehouse.educationinstructor.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;

class EducationInstructorTest {

    private static final UUID EDUCATION_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_ID    = new CompositeKey(EDUCATION_REF, INSTRUCTOR_REF);
    private static final CreatedAtTimestamp CREATED_AT = new CreatedAtTimestamp(LocalDateTime.now().minusDays(1));
    private static final CreatedAtTimestamp NEW_CREATED_AT = new CreatedAtTimestamp(LocalDateTime.now().minusDays(2));

    // ── Builder happy path ─────────────────────────────────────────────────────


    @Test
    void builder_shouldCreateAggregate_whenAllFieldsAreValid() {
        EducationInstructor aggregate = EducationInstructor.builder()
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
        EducationInstructor aggregate = EducationInstructor.builder()
                .id(VALID_ID)
                .active(false)
                .createdAt(CREATED_AT)
                .build();

        assertFalse(aggregate.isActive());
    }

    @Test
    void builder_shouldCreateAggregate_whenCreatedAtIsNull() {
        // createdAt is validated in its own value object — builder allows null
        EducationInstructor aggregate = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(null)
                .build();

        assertNull(aggregate.getCreatedAt());
    }

    @Test
    void builder_shouldCreateAggregate_whenIdIsNull() {
        // id is validated in its own value object — builder allows null
        EducationInstructor aggregate = EducationInstructor.builder()
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
                EducationInstructor.builder()
                        .id(VALID_ID)
                        .createdAt(CREATED_AT)
                        .build());

        assertEquals("educationinstructor.active.required", ex.getMessage());
    }

    // ── withActive ─────────────────────────────────────────────────────────────

    @Test
    void withActive_shouldReturnNewInstance_withUpdatedActive() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        EducationInstructor updated = original.withActive(false);

        assertFalse(updated.isActive());
    }

    @Test
    void withActive_shouldReturnNewInstance_notSameReference() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        EducationInstructor updated = original.withActive(false);

        assertNotSame(original, updated);
    }

    @Test
    void withActive_shouldPreserveId_andCreatedAt() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        EducationInstructor updated = original.withActive(false);

        assertEquals(VALID_ID, updated.getId());
        assertEquals(CREATED_AT, updated.getCreatedAt());
    }

    // ── withCreatedAt ──────────────────────────────────────────────────────────

    @Test
    void withCreatedAt_shouldReturnNewInstance_withUpdatedCreatedAt() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        EducationInstructor updated = original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(NEW_CREATED_AT, updated.getCreatedAt());
    }

    @Test
    void withCreatedAt_shouldReturnNewInstance_notSameReference() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        EducationInstructor updated = original.withCreatedAt(NEW_CREATED_AT);

        assertNotSame(original, updated);
    }

    @Test
    void withCreatedAt_shouldPreserveId_andActive() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        EducationInstructor updated = original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(VALID_ID, updated.getId());
        assertTrue(updated.isActive());
    }

    // ── Immutability ───────────────────────────────────────────────────────────

    @Test
    void withActive_shouldNotMutateOriginal() {
        EducationInstructor original = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        original.withActive(false);

        assertTrue(original.isActive());
    }

    @Test
    void withCreatedAt_shouldNotMutateOriginal() {
        EducationInstructor original = EducationInstructor.builder()
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
        EducationInstructor aggregate = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(VALID_ID, aggregate.getId());
    }

    @Test
    void isActive_shouldReturnCorrectValue() {
        EducationInstructor aggregate = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertTrue(aggregate.isActive());
    }

    @Test
    void getCreatedAt_shouldReturnCorrectValue() {
        EducationInstructor aggregate = EducationInstructor.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(CREATED_AT, aggregate.getCreatedAt());
    }
}
