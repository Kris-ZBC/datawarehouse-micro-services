package local.sop.sopinfo.educationline.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineCreatedAt;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;

class EducationLineJpaMapperTest {

    @Test
    void toDomain_maps_entity_to_aggregate() {
        UUID id = UUID.randomUUID();
        UUID educationRef = UUID.randomUUID();
        EducationLineEntity entity = new EducationLineEntity(id, "Math", 1, 2, 3, educationRef, true);

        EducationLineJpaMapper mapper = new EducationLineJpaMapper();
        EducationLine domain = mapper.toDomain(entity);

        assertEquals(id, domain.getId().value());
        assertEquals("Math", domain.getName().value());
        assertEquals(1, domain.getDuration().years());
        assertEquals(2, domain.getDuration().months());
        assertEquals(3, domain.getDuration().days());
        assertEquals(educationRef, domain.getEducationRef().value());
        assertEquals(true, domain.isActive());
        assertNotNull(domain.getCreatedAt());
    }

    @Test
    void toEntity_maps_aggregate_to_entity() {
        UUID id = UUID.randomUUID();
        UUID educationRef = UUID.randomUUID();
        EducationLine aggregate = EducationLine.builder()
            .id(new EducationLineId(id))
            .name(new EducationLineName("Physics"))
            .duration(new EducationLineDuration(2, 3, 4))
            .createdAt(new EducationLineCreatedAt(Instant.now()))
            .educationRef(new EducationRef(educationRef))
            .active(false)
            .build();

        EducationLineJpaMapper mapper = new EducationLineJpaMapper();
        EducationLineEntity entity = mapper.toEntity(aggregate);

        assertEquals(id, entity.getId());
        assertEquals("Physics", entity.getName());
        assertEquals(2, entity.getDurationYears());
        assertEquals(3, entity.getDurationMonths());
        assertEquals(4, entity.getDurationDays());
        assertEquals(educationRef, entity.getEducationRef());
        assertEquals(false, entity.isActive());
    }

    @Test
    void toDomainList_maps_all_entities() {
        EducationLineJpaMapper mapper = new EducationLineJpaMapper();
        List<EducationLineEntity> entities = List.of(
            new EducationLineEntity(UUID.randomUUID(), "Math", 1, 0, 1, UUID.randomUUID(), true),
            new EducationLineEntity(UUID.randomUUID(), "Physics", 2, 3, 4, UUID.randomUUID(), false)
        );

        List<EducationLine> result = mapper.toDomainList(entities);

        assertEquals(2, result.size());
        assertEquals("Math", result.get(0).getName().value());
        assertEquals("Physics", result.get(1).getName().value());
    }
}
