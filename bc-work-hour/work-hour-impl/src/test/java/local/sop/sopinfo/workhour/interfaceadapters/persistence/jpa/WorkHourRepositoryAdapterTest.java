package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.workhour.domain.model.WorkHour;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkHourId;

@DataJpaTest
@TestPropertySource(properties = {"security.enabled=false"})
@ActiveProfiles({"test", "h2"})
@Import({WorkHourRepositoryAdapter.class,WorkHourMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkHourRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkHourRepositoryAdapter adapter;

    // ── Helper ───────────────────────────────────────────────────────────────

    private WorkHourEntity persistEntity(UUID id,LocalTime startTime,LocalTime endTime) {

        WorkHourEntity entity = WorkHourEntity.builder()
                .id(id)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        return entity;
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnDomain_whenEntityExists() {

        UUID id = UUID.randomUUID();

        persistEntity(id,LocalTime.of(7, 45),LocalTime.of(15, 45));

        Optional<WorkHour> result = adapter.findById(WorkHourId.of(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals(LocalTime.of(7, 45), result.get().getStartTime());
        assertEquals(LocalTime.of(15, 45), result.get().getEndTime());
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {

        Optional<WorkHour> result = adapter.findById(WorkHourId.of(UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }

    // ── save ────────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDomain() {

        UUID id = UUID.randomUUID();

        WorkHour domain = WorkHour.builder()
                .id(WorkHourId.of(id))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        WorkHour saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(id, saved.getId().value());
        assertEquals(LocalTime.of(8, 0), saved.getStartTime());
        assertEquals(LocalTime.of(16, 0), saved.getEndTime());
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateTimes_whenEntityExists() {

        UUID id = UUID.randomUUID();

        persistEntity(id, LocalTime.of(7, 0), LocalTime.of(15, 0));

        WorkHour updated = WorkHour.builder()
                .id(WorkHourId.of(id))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        adapter.update(updated);

        entityManager.flush();
        entityManager.clear();

        Optional<WorkHour> result = adapter.findById(WorkHourId.of(id));

        assertTrue(result.isPresent());
        assertEquals(LocalTime.of(8, 0), result.get().getStartTime());
        assertEquals(LocalTime.of(16, 0), result.get().getEndTime());
    }

    @Test
    void update_shouldThrowValidationException_whenEntityDoesNotExist() {

        WorkHour workHour = WorkHour.builder()
                .id(WorkHourId.of(UUID.randomUUID()))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();

        assertThrows(ValidationException.class, () -> adapter.update(workHour));
    }

    // ── getAll ──────────────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturnAllEntities() {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        persistEntity(id1, LocalTime.of(7, 0), LocalTime.of(15, 0));
        persistEntity(id2, LocalTime.of(8, 0), LocalTime.of(16, 0));
        persistEntity(id3, LocalTime.of(9, 0), LocalTime.of(17, 0));

        List<WorkHour> result = adapter.getAll();

        assertEquals(3, result.size());

        assertTrue(result.stream().anyMatch(w -> w.getId().value().equals(id1)));

        assertTrue(result.stream().anyMatch(w -> w.getId().value().equals(id2)));

        assertTrue(result.stream().anyMatch(w -> w.getId().value().equals(id3)));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoEntitiesExist() {

        List<WorkHour> result = adapter.getAll();

        assertTrue(result.isEmpty());
    }

}
