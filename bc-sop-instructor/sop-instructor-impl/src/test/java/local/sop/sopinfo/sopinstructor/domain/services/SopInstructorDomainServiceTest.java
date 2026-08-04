package local.sop.sopinfo.sopinstructor.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sopinstructor.domain.service.SopInstructorDomainService;

class SopInstructorDomainServiceTest {

    private final SopInstructorDomainService service = new SopInstructorDomainService();

    private static final UUID SOP_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, INSTRUCTOR_REF);

    // ── createSopInstructor ────────────────────────────────────────────────────

    @Test
    void createSopInstructor_shouldReturnAggregate_withCorrectId() {
        SopInstructor result = service.createSopInstructor(VALID_KEY, true);
        assertEquals(VALID_KEY, result.getId());
    }

    @Test
    void createSopInstructor_shouldReturnAggregate_withActiveTrue() {
        SopInstructor result = service.createSopInstructor(VALID_KEY, true);
        assertTrue(result.isActive());
    }

    @Test
    void createSopInstructor_shouldReturnAggregate_withActiveFalse() {
        SopInstructor result = service.createSopInstructor(VALID_KEY, false);
        assertFalse(result.isActive());
    }

    @Test
    void createSopInstructor_shouldSetCreatedAt_toNow() {
        SopInstructor result = service.createSopInstructor(VALID_KEY, true);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedAt().value());
    }

    // ── toggleActivateSopInstructor ────────────────────────────────────────────

    @Test
    void toggleActivateSopInstructor_shouldReturnAggregate_withToggledActiveFromTrue() {
        SopInstructor result = service.toggleActivateSopInstructor(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertFalse(result.isActive());
    }

    @Test
    void toggleActivateSopInstructor_shouldReturnAggregate_withToggledActiveFromFalse() {
        SopInstructor result = service.toggleActivateSopInstructor(VALID_KEY, false, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertTrue(result.isActive());
    }

    @Test
    void toggleActivateSopInstructor_shouldPreserveId() {
        SopInstructor result = service.toggleActivateSopInstructor(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertEquals(VALID_KEY, result.getId());
    }

    // ── deleteSopInstructor ────────────────────────────────────────────────────

    @Test
    void deleteSopInstructor_shouldReturnAggregate_withActiveFalse() {
        SopInstructor result = service.deleteSopInstructor(VALID_KEY);
        assertFalse(result.isActive());
    }

    @Test
    void deleteSopInstructor_shouldPreserveId() {
        SopInstructor result = service.deleteSopInstructor(VALID_KEY);
        assertEquals(VALID_KEY, result.getId());
    }

}
