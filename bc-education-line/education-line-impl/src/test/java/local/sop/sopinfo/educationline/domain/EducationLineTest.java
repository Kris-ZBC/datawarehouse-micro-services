package local.sop.sopinfo.educationline.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineCreatedAt;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class EducationLineTest {

    private final EducationLineId id = new EducationLineId(UUID.randomUUID());
    private final EducationLineName name = new EducationLineName("Mathematics");
    private final EducationLineDuration duration = new EducationLineDuration(1, 2, 3);
    private final EducationLineCreatedAt createdAt = new EducationLineCreatedAt(Instant.now());
    private final EducationRef educationRef = new EducationRef(UUID.randomUUID());
    private final Boolean active = true;

    @Test
    void happyPath_constructor_succeeds() {
        EducationLine line = new EducationLine(id, name, duration, createdAt, educationRef, active);
        assertEquals(id, line.getId());
        assertEquals(name, line.getName());
        assertEquals(duration, line.getDuration());
        assertEquals(createdAt, line.getCreatedAt());
        assertEquals(educationRef, line.getEducationRef());
        assertEquals(active, line.isActive());
    }

    @Test
    void happyPath_create_succeeds_and_defaults() {
        EducationLine line = EducationLine.create(null, name, duration, null, educationRef, active);
        assertNotNull(line.getId());
        assertNotNull(line.getCreatedAt());
        assertEquals(name, line.getName());
        assertEquals(duration, line.getDuration());
        assertEquals(educationRef, line.getEducationRef());
        assertEquals(active, line.isActive());
    }

    @Test
    void happyPath_builder_succeeds() {
        EducationLine line = EducationLine.builder()
            .id(id)
            .name(name)
            .duration(duration)
            .createdAt(createdAt)
            .educationRef(educationRef)
            .active(active)
            .build();
        assertEquals(id, line.getId());
        assertEquals(name, line.getName());
        assertEquals(duration, line.getDuration());
        assertEquals(createdAt, line.getCreatedAt());
        assertEquals(educationRef, line.getEducationRef());
        assertEquals(active, line.isActive());
    }

    @Test
    void unhappyPath_null_id_fails() {
        assertThrows(ValidationException.class, () -> new EducationLine(null, name, duration, createdAt, educationRef, active));
    }

    @Test
    void unhappyPath_null_name_fails() {
        assertThrows(ValidationException.class, () -> new EducationLine(id, null, duration, createdAt, educationRef, active));
    }

    @Test
    void unhappyPath_null_duration_fails() {
        assertThrows(ValidationException.class, () -> new EducationLine(id, name, null, createdAt, educationRef, active));
    }

    @Test
    void unhappyPath_null_createdAt_fails() {
        assertThrows(ValidationException.class, () -> new EducationLine(id, name, duration, null, educationRef, active));
    }

    @Test
    void unhappyPath_null_educationRef_fails() {
        assertThrows(ValidationException.class, () -> new EducationLine(id, name, duration, createdAt, null, active));
    }

    @Test
    void unhappyPath_null_active_fails() {
        assertThrows(ValidationException.class, () -> new EducationLine(id, name, duration, createdAt, educationRef, null));
    }
}
