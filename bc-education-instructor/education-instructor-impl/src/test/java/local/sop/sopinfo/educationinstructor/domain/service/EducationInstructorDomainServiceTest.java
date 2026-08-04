package local.sop.sopinfo.educationinstructor.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.sopinfo.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

class EducationInstructorDomainServiceTest {

    private final EducationInstructorDomainService service = new EducationInstructorDomainService();

    private static final UUID EDUCATION_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(EDUCATION_REF, INSTRUCTOR_REF);

    // ── createEducationInstructor ────────────────────────────────────────────────────

    @Test
    void createEducationInstructor_shouldReturnAggregate_withCorrectId() {
        EducationInstructor result = service.createEducationInstructor(VALID_KEY, true);
        assertEquals(VALID_KEY, result.getId());
    }

    @Test
    void createEducationInstructor_shouldReturnAggregate_withActiveTrue() {
        EducationInstructor result = service.createEducationInstructor(VALID_KEY, true);
        assertTrue(result.isActive());
    }

    @Test
    void createEducationInstructor_shouldReturnAggregate_withActiveFalse() {
        EducationInstructor result = service.createEducationInstructor(VALID_KEY, false);
        assertFalse(result.isActive());
    }

    @Test
    void createEducationInstructor_shouldSetCreatedAt_toNow() {
        EducationInstructor result = service.createEducationInstructor(VALID_KEY, true);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedAt().value());
    }

    // ── toggleActivateEducationInstructor ────────────────────────────────────────────

    @Test
    void toggleActivateEducationInstructor_shouldReturnAggregate_withToggledActiveFromTrue() {
        EducationInstructor result = service.toggleActivateEducationInstructor(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertFalse(result.isActive());
    }

    @Test
    void toggleActivateEducationInstructor_shouldReturnAggregate_withToggledActiveFromFalse() {
        EducationInstructor result = service.toggleActivateEducationInstructor(VALID_KEY, false, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertTrue(result.isActive());
    }

    @Test
    void toggleActivateEducationInstructor_shouldPreserveId() {
        EducationInstructor result = service.toggleActivateEducationInstructor(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertEquals(VALID_KEY, result.getId());
    }

    // ── deleteEducationInstructor ────────────────────────────────────────────────────

    @Test
    void deleteEducationInstructor_shouldReturnAggregate_withActiveFalse() {
        EducationInstructor result = service.deleteEducationInstructor(VALID_KEY);
        assertFalse(result.isActive());
    }

    @Test
    void deleteEducationInstructor_shouldPreserveId() {
        EducationInstructor result = service.deleteEducationInstructor(VALID_KEY);
        assertEquals(VALID_KEY, result.getId());
    }
}
