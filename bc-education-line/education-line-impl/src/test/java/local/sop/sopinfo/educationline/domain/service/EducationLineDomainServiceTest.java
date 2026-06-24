package local.sop.sopinfo.educationline.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineCreatedAt;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;

class EducationLineDomainServiceTest {

    @Test
    void createEducationLine_returns_new_id_and_copies_fields() {
        EducationLineDomainService service = new EducationLineDomainService();
        EducationLine input = EducationLine.builder()
            .id(new EducationLineId(UUID.randomUUID()))
            .name(new EducationLineName("Mathematics"))
            .duration(new EducationLineDuration(1, 2, 3))
            .createdAt(new EducationLineCreatedAt(Instant.now()))
            .educationRef(new EducationRef(UUID.randomUUID()))
            .active(true)
            .build();

        EducationLine result = service.createEducationLine(input);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotEquals(input.getId(), result.getId());
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getDuration(), result.getDuration());
        assertEquals(input.getCreatedAt(), result.getCreatedAt());
        assertEquals(input.getEducationRef(), result.getEducationRef());
        assertEquals(input.isActive(), result.isActive());
    }
}
