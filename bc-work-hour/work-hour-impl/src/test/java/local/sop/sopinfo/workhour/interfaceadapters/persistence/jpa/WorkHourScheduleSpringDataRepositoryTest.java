package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import jakarta.transaction.Transactional;
import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "bc.qualifier=work_hour"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class WorkHourScheduleSpringDataRepositoryTest {
    
    @Autowired
    private WorkHourScheduleSpringDataRepository repository;

    private WorkHourScheduleEntity testEntity1;
    private WorkHourScheduleEntity testEntity2;
    private UUID sopId1;
    private UUID sopId2;

    @BeforeEach
    void setUp() {
        // Opret testdata
        sopId1 = UUIDUtil.newUuid();
        sopId2 = UUIDUtil.newUuid();

        testEntity1 = WorkHourScheduleEntity.builder()
            .id(UUIDUtil.newUuid())
            .startTime("08:00")
            .endTime("16:00")
            .weekDay(WeekDay.MONDAY)
            .sopRef(sopId1)
            .build();

        testEntity2 = WorkHourScheduleEntity.builder()
            .id(UUIDUtil.newUuid())
            .startTime("09:00")
            .endTime("17:00")
            .weekDay(WeekDay.TUESDAY)
            .sopRef(sopId2)
            .build();

        repository.save(testEntity1);
        repository.save(testEntity2);
    }

    @Test
    void testFindById() {
        Optional<WorkHourScheduleEntity> found = repository.findById(testEntity1.getId());
        
        assertTrue(found.isPresent());
        assertEquals(testEntity1.getId(), found.get().getId());
        assertEquals("08:00", found.get().getStartTime());
        assertEquals(WeekDay.MONDAY, found.get().getWeekDay());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<WorkHourScheduleEntity> notFound = repository.findById(UUIDUtil.newUuid());
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindBySearchParamsWithAllParameters() {
        List<WorkHourScheduleEntity> results = repository.findBySearchParams(
            testEntity1.getId(),
            "08:00",
            "16:00",
            WeekDay.MONDAY,
            sopId1
        );

        assertEquals(1, results.size());
        assertEquals(testEntity1.getId(), results.get(0).getId());
    }

    @Test
    void testFindBySearchParamsWithPartialParameters() {
        List<WorkHourScheduleEntity> results = repository.findBySearchParams(
            null, // id
            "08:00",
            null, // endTime
            WeekDay.MONDAY,
            sopId1
        );

        assertEquals(1, results.size());
        assertEquals(testEntity1.getId(), results.get(0).getId());
    }

    @Test
    void testFindBySearchParamsOnlySopRef() {
        List<WorkHourScheduleEntity> results = repository.findBySearchParams(
            null,
            null,
            null,
            null,
            sopId1
        );

        assertEquals(1, results.size());
        assertEquals(testEntity1.getId(), results.get(0).getId());
    }

    @Test
    void testFindBySearchParamsAllNullReturnsAll() {
        List<WorkHourScheduleEntity> results = repository.findBySearchParams(
            null,
            null,
            null,
            null,
            null
        );

        assertEquals(2, results.size());
    }

    @Test
    void testFindBySearchParamsNoMatch() {
        List<WorkHourScheduleEntity> results = repository.findBySearchParams(
            UUID.randomUUID(), // non-existing id
            "10:00", // non-existing time
            "18:00",
            WeekDay.WEDNESDAY, // non-existing day
            UUID.randomUUID() // non-existing sopRef
        );

        assertTrue(results.isEmpty());
    }

    @Test
    void testFindBySearchParamsMultipleMatches() {
        // Tilføj en entity med samme sopRef men anden ugedag
        WorkHourScheduleEntity testEntity3 = WorkHourScheduleEntity.builder()
            .id(UUID.randomUUID())
            .startTime("08:00")
            .endTime("16:00")
            .weekDay(WeekDay.WEDNESDAY)
            .sopRef(sopId1) // samme sopRef som testEntity1
            .build();
        repository.save(testEntity3);

        List<WorkHourScheduleEntity> results = repository.findBySearchParams(
            null,
            null,
            null,
            null,
            sopId1 // skal finde 2 entities med samme sopRef
        );

        assertEquals(2, results.size());
    }

    @Test
    void testCrudOperations() {
        // Create
        WorkHourScheduleEntity newEntity = WorkHourScheduleEntity.builder()
            .id(UUID.randomUUID())
            .startTime("10:00")
            .endTime("18:00")
            .weekDay(WeekDay.FRIDAY)
            .sopRef(UUID.randomUUID())
            .build();
        
        WorkHourScheduleEntity saved = repository.save(newEntity);
        assertNotNull(saved.getId());
        assertEquals(newEntity.getId(), saved.getId());

        // Read
        Optional<WorkHourScheduleEntity> found = repository.findById(newEntity.getId());
        assertTrue(found.isPresent());

       // Update
        repository.updateWorkHourSchedule(
            newEntity.getId(), "11:00", "18:00", WeekDay.FRIDAY, newEntity.getSopRef()
        );

        Optional<WorkHourScheduleEntity> updatedFound = repository.findById(newEntity.getId());
        assertEquals("11:00", updatedFound.get().getStartTime());

        // Delete
        repository.deleteById(newEntity.getId());
        assertFalse(repository.findById(newEntity.getId()).isPresent());
    }

    @Test
    void testCount() {
        long count = repository.count();
        assertEquals(2, count); // Vi har 2 entities fra setUp
    }


}
