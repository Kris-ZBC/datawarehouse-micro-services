// ── SopEducationDomainServiceTest.java ────────────────────────────────────────

package local.sop.sopinfo.sopeducation.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;
import local.sop.sopinfo.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;

class SopEducationDomainServiceTest {

    private final SopEducationDomainService service = new SopEducationDomainService();

    private static final UUID SOP_REF      = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, EDUCATION_REF);

    // ── createSopEducation ────────────────────────────────────────────────────

    @Test
    void createSopEducation_shouldReturnAggregate_withCorrectId() {
        SopEducation result = service.createSopEducation(VALID_KEY, true);
        assertEquals(VALID_KEY, result.getId());
    }

    @Test
    void createSopEducation_shouldReturnAggregate_withActiveTrue() {
        SopEducation result = service.createSopEducation(VALID_KEY, true);
        assertTrue(result.isActive());
    }

    @Test
    void createSopEducation_shouldReturnAggregate_withActiveFalse() {
        SopEducation result = service.createSopEducation(VALID_KEY, false);
        assertFalse(result.isActive());
    }

    @Test
    void createSopEducation_shouldSetCreatedAt_toNow() {
        SopEducation result = service.createSopEducation(VALID_KEY, true);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedAt().value());
    }

    // ── toggleActivateSopEducation ────────────────────────────────────────────

    @Test
    void toggleActivateSopEducation_shouldReturnAggregate_withToggledActiveFromTrue() {
        SopEducation result = service.toggleActivateSopEducation(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertFalse(result.isActive());
    }

    @Test
    void toggleActivateSopEducation_shouldReturnAggregate_withToggledActiveFromFalse() {
        SopEducation result = service.toggleActivateSopEducation(VALID_KEY, false, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertTrue(result.isActive());
    }

    @Test
    void toggleActivateSopEducation_shouldPreserveId() {
        SopEducation result = service.toggleActivateSopEducation(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertEquals(VALID_KEY, result.getId());
    }

    // ── deleteSopEducation ────────────────────────────────────────────────────

    @Test
    void deleteSopEducation_shouldReturnAggregate_withActiveFalse() {
        SopEducation result = service.deleteSopEducation(VALID_KEY);
        assertFalse(result.isActive());
    }

    @Test
    void deleteSopEducation_shouldPreserveId() {
        SopEducation result = service.deleteSopEducation(VALID_KEY);
        assertEquals(VALID_KEY, result.getId());
    }
}