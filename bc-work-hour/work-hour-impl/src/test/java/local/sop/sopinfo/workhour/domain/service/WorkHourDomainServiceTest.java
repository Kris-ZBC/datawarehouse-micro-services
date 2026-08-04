package local.sop.sopinfo.workhour.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.workhour.domain.model.WorkHour;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkHourId;

class WorkHourDomainServiceTest {

    private final WorkHourDomainService service = new WorkHourDomainService();

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_shouldReturnAggregate_withGeneratedId() {
        WorkHour result = service.create(LocalTime.of(7, 45),LocalTime.of(15, 45));

        assertNotNull(result.getId());
    }

    @Test
    void create_shouldReturnAggregate_withCorrectStartTime() {
        WorkHour result = service.create(LocalTime.of(7, 45),LocalTime.of(15, 45));

        assertEquals(LocalTime.of(7, 45), result.getStartTime());
    }

    @Test
    void create_shouldReturnAggregate_withCorrectEndTime() {
        WorkHour result = service.create(LocalTime.of(7, 45),LocalTime.of(15, 45));

        assertEquals(LocalTime.of(15, 45), result.getEndTime());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_shouldReturnAggregate_withProvidedId() {

        WorkHourId id = WorkHourId.newId();

        WorkHour result = service.update(id, LocalTime.of(8, 0), LocalTime.of(17, 0));

        assertEquals(id, result.getId());
    }

    @Test
    void update_shouldReturnAggregate_withUpdatedStartTime() {

        WorkHourId id = WorkHourId.newId();

        WorkHour result = service.update(id,LocalTime.of(8, 0),LocalTime.of(17, 0));

        assertEquals(LocalTime.of(8, 0), result.getStartTime());
    }

    @Test
    void update_shouldReturnAggregate_withUpdatedEndTime() {

        WorkHourId id = WorkHourId.newId();

        WorkHour result = service.update(id,LocalTime.of(8, 0),LocalTime.of(17, 0));

        assertEquals(LocalTime.of(17, 0), result.getEndTime());
    }
}