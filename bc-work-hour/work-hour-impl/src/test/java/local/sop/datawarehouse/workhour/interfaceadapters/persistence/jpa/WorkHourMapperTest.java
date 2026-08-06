package local.sop.datawarehouse.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.workhour.domain.model.WorkHour;
import local.sop.datawarehouse.workhour.domain.model.valueobjects.WorkHourId;

class WorkHourMapperTest {

    private final WorkHourMapper mapper = new WorkHourMapper();

    @Test
    void toEntity_and_toDomain_preserveImportantFields() {

        UUID id = UUID.randomUUID();

        WorkHour domain = WorkHour.builder()
            .id(WorkHourId.of(id))
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build();

        WorkHourEntity entity = mapper.toEntity(domain);
        WorkHour mappedBack = mapper.toDomain(entity);

        assertEquals(id, mappedBack.getId().value());
        assertEquals(LocalTime.of(7, 45), mappedBack.getStartTime());
        assertEquals(LocalTime.of(15, 45), mappedBack.getEndTime());
    }

    @Test
    void toDomain_mapsEntityCorrectly() {

        UUID id = UUID.randomUUID();

        WorkHourEntity entity = WorkHourEntity.builder()
            .id(id)
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(16, 0))
            .build();

        WorkHour domain = mapper.toDomain(entity);

        assertEquals(id, domain.getId().value());
        assertEquals(LocalTime.of(8, 0), domain.getStartTime());
        assertEquals(LocalTime.of(16, 0), domain.getEndTime());
    }

    @Test
    void toEntity_mapsDomainCorrectly() {

        UUID id = UUID.randomUUID();

        WorkHour domain = WorkHour.builder()
            .id(WorkHourId.of(id))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(17, 0))
            .build();

        WorkHourEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals(LocalTime.of(9, 0), entity.getStartTime());
        assertEquals(LocalTime.of(17, 0), entity.getEndTime());
    }

    @Test
    void copyIntoEntity_updatesFields() {

        UUID id = UUID.randomUUID();

        WorkHour domain = WorkHour.builder()
            .id(WorkHourId.of(id))
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(16, 0))
            .build();

        WorkHourEntity entity = WorkHourEntity.builder()
            .id(id)
            .startTime(LocalTime.of(7, 0))
            .endTime(LocalTime.of(15, 0))
            .build();

        mapper.copyIntoEntity(domain, entity);

        assertEquals(LocalTime.of(8, 0), entity.getStartTime());
        assertEquals(LocalTime.of(16, 0), entity.getEndTime());
    }
}