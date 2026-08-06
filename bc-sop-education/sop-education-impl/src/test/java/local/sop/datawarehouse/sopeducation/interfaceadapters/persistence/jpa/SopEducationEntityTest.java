package local.sop.datawarehouse.sopeducation.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class SopEducationEntityTest {

    private static final UUID SOP_REF           = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final SopEducationId VALID_ID = new SopEducationId(SOP_REF, EDUCATION_REF);

    // ── Builder happy path ─────────────────────────────────────────────────────

    @Test
    void builder_shouldCreateEntity_whenAllFieldsAreValid() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        assertEquals(VALID_ID, entity.getId());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void builder_shouldSetCreatedAt_toCurrentTime() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(entity.getCreatedAt().isAfter(before));
        assertTrue(entity.getCreatedAt().isBefore(after));
    }

    @Test
    void builder_shouldCreateEntity_whenActiveIsFalse() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(false)
                .build();

        assertFalse(entity.isActive());
    }

    // ── Builder unhappy path ───────────────────────────────────────────────────

    @Test
    void builder_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                SopEducationEntity.builder()
                        .active(true)
                        .build());

        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void builder_shouldThrowValidationException_whenActiveIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                SopEducationEntity.builder()
                        .id(VALID_ID)
                        .build());

        assertEquals("sop-education.active.required", ex.getMessage());
    }

    // ── Wither ─────────────────────────────────────────────────────────────────

    @Test
    void withActive_shouldUpdateActiveToTrue() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(false)
                .build();

        SopEducationEntity updated = entity.withActive(true);

        assertTrue(updated.isActive());
    }

    @Test
    void withActive_shouldUpdateActiveToFalse() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        SopEducationEntity updated = entity.withActive(false);

        assertFalse(updated.isActive());
    }

    @Test
    void withActive_shouldReturnSameInstance() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        SopEducationEntity updated = entity.withActive(false);

        assertSame(entity, updated);
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    @Test
    void getId_shouldReturnCorrectId() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        assertEquals(VALID_ID, entity.getId());
    }

    @Test
    void isActive_shouldReturnCorrectValue() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        assertTrue(entity.isActive());
    }

    @Test
    void getCreatedAt_shouldNotBeNull() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(VALID_ID)
                .active(true)
                .build();

        assertNotNull(entity.getCreatedAt());
    }
}
