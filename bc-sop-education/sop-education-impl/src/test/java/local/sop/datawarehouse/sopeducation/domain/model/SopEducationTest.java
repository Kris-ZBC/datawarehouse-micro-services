package local.sop.datawarehouse.sopeducation.domain.model;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;


public class SopEducationTest {

    private static final UUID SOP_REF            = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF  = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final CompositeKey VALID_ID    = new CompositeKey(SOP_REF, EDUCATION_REF);
    private static final CreatedAtTimestamp CREATED_AT = new CreatedAtTimestamp(LocalDateTime.now().minusDays(1));
    private static final CreatedAtTimestamp NEW_CREATED_AT = new CreatedAtTimestamp(LocalDateTime.now().minusDays(2));

    // ── Builder happy path ─────────────────────────────────────────────────────

    @Test
    void builder_shouldCreateAggregate_whenAllFieldsAreValid() {
        SopEducation aggregate = SopEducation.builder()
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
        SopEducation aggregate = SopEducation.builder()
                .id(VALID_ID)
                .active(false)
                .createdAt(CREATED_AT)
                .build();

        assertFalse(aggregate.isActive());
    }

    @Test
    void builder_shouldCreateAggregate_whenCreatedAtIsNull() {
        // createdAt is validated in its own value object — builder allows null
        SopEducation aggregate = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(null)
                .build();

        assertNull(aggregate.getCreatedAt());
    }

    @Test
    void builder_shouldCreateAggregate_whenIdIsNull() {
        // id is validated in its own value object — builder allows null
        SopEducation aggregate = SopEducation.builder()
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
                SopEducation.builder()
                        .id(VALID_ID)
                        .createdAt(CREATED_AT)
                        .build());

        assertEquals("sop-education.active.required", ex.getMessage());
    }

    // ── withActive ─────────────────────────────────────────────────────────────

    @Test
    void withActive_shouldReturnNewInstance_withUpdatedActive() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        SopEducation updated = original.withActive(false);

        assertFalse(updated.isActive());
    }

    @Test
    void withActive_shouldReturnNewInstance_notSameReference() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        SopEducation updated = original.withActive(false);

        assertNotSame(original, updated);
    }

    @Test
    void withActive_shouldPreserveId_andCreatedAt() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        SopEducation updated = original.withActive(false);

        assertEquals(VALID_ID, updated.getId());
        assertEquals(CREATED_AT, updated.getCreatedAt());
    }

    // ── withCreatedAt ──────────────────────────────────────────────────────────

    @Test
    void withCreatedAt_shouldReturnNewInstance_withUpdatedCreatedAt() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

       SopEducation updated = original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(NEW_CREATED_AT, updated.getCreatedAt());
    }

    @Test
    void withCreatedAt_shouldReturnNewInstance_notSameReference() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        SopEducation updated = original.withCreatedAt(NEW_CREATED_AT);
               

        assertNotSame(original, updated);
    }

    @Test
    void withCreatedAt_shouldPreserveId_andActive() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        SopEducation updated = original.withCreatedAt(NEW_CREATED_AT);

        assertEquals(VALID_ID, updated.getId());
        assertTrue(updated.isActive());
    }

    // ── Immutability ───────────────────────────────────────────────────────────

    @Test
    void withActive_shouldNotMutateOriginal() {
        SopEducation original = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        original.withActive(false);

        assertTrue(original.isActive());
    }

    @Test
    void withCreatedAt_shouldNotMutateOriginal() {
        SopEducation original = SopEducation.builder()
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
        SopEducation aggregate = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(VALID_ID, aggregate.getId());
    }

    @Test
    void isActive_shouldReturnCorrectValue() {
        SopEducation aggregate = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertTrue(aggregate.isActive());
    }

    @Test
    void getCreatedAt_shouldReturnCorrectValue() {
        SopEducation aggregate = SopEducation.builder()
                .id(VALID_ID)
                .active(true)
                .createdAt(CREATED_AT)
                .build();

        assertEquals(CREATED_AT, aggregate.getCreatedAt());
    }
}
