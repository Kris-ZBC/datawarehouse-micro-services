package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

@ExtendWith(MockitoExtension.class)
class WorkHourScheduleRepositoryAdapterTest {

    @Mock
    private WorkHourScheduleSpringDataRepository jpaRepo;

    @Mock
    private WorkHourScheduleMapper mapper;

    @InjectMocks
    private WorkHourScheduleRepositoryAdapter repo;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private WorkHourScheduleEntity buildEntity(UUID id, UUID sopRef) {
        return WorkHourScheduleEntity.builder()
            .id(id)
            .startTime("07:00")
            .endTime("16:00")
            .weekDay(WeekDay.MONDAY)
            .sopRef(sopRef)
            .build();
    }

    private WorkHourSchedule buildSchedule(UUID id, UUID sopRef) {
        return WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of("07:00"))
            .endTime(WorkScheduleTime.of("16:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(SopRef.of(sopRef))
            .build();
    }

    // ---------------------------------------------------------------------------
    // save
    // ---------------------------------------------------------------------------

    @Test
    void happyPath_save_shouldCreateNewInstance() {
        UUID savedId = UUIDUtil.newUuid();
        UUID sopRef  = UUIDUtil.newUuid();

        WorkHourSchedule schedule = buildSchedule(savedId, sopRef);
        WorkHourScheduleEntity entity = buildEntity(savedId, sopRef);
        WorkHourSchedule domain = buildSchedule(savedId, sopRef);

        Mockito.when(mapper.toEntity(schedule)).thenReturn(entity);
        Mockito.when(jpaRepo.save(entity)).thenReturn(entity);
        Mockito.when(jpaRepo.findById(savedId)).thenReturn(Optional.of(entity));
        Mockito.when(mapper.toDomain(entity)).thenReturn(domain);

        WorkHourSchedule result = repo.save(schedule);

        assertNotNull(result);
        assertEquals(savedId, result.getId().value());
        Mockito.verify(jpaRepo).save(entity);
        Mockito.verify(jpaRepo).findById(savedId);
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }

    // ---------------------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------------------

    @Test
    void happyPath_findById_shouldReturnAggregateRoot() {
        UUID id     = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        WorkHourScheduleEntity entity = buildEntity(id, sopRef);
        WorkHourSchedule domain = buildSchedule(id, sopRef);

        Mockito.when(jpaRepo.findBySearchParams(id, null, null, null, null))
            .thenReturn(List.of(entity));
        Mockito.when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<WorkHourSchedule> result = repo.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        Mockito.verify(jpaRepo).findBySearchParams(id, null, null, null, null);
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }

    @Test
    void happyPath_findById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUIDUtil.newUuid();

        Mockito.when(jpaRepo.findBySearchParams(id, null, null, null, null))
            .thenReturn(List.of());

        Optional<WorkHourSchedule> result = repo.findById(id);

        assertEquals(Optional.empty(), result);
        Mockito.verify(jpaRepo).findBySearchParams(id, null, null, null, null);
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }

    // ---------------------------------------------------------------------------
    // findBySearchParams
    // ---------------------------------------------------------------------------

    @Test
    void happyPath_findBySearchParams_shouldReturnMappedList() {
        UUID sopRef = UUIDUtil.newUuid();
        List<WorkHourScheduleEntity> entities = List.of(
            buildEntity(UUIDUtil.newUuid(), sopRef),
            buildEntity(UUIDUtil.newUuid(), sopRef),
            buildEntity(UUIDUtil.newUuid(), sopRef)
        );

        Mockito.when(jpaRepo.findBySearchParams(null, null, null, null, null))
            .thenReturn(entities);
        entities.forEach(e ->
            Mockito.when(mapper.toDomain(e)).thenReturn(buildSchedule(e.getId(), sopRef))
        );

        List<WorkHourSchedule> result = repo.findBySearchParams(null, null, null, null, null);

        assertNotNull(result);
        assertEquals(3, result.size());
        Mockito.verify(jpaRepo).findBySearchParams(null, null, null, null, null);
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }

    // ---------------------------------------------------------------------------
    // update
    // ---------------------------------------------------------------------------

    @Test
    void happyPath_update_shouldUpdateExistingSchedule() {
        UUID id     = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        WorkHourSchedule updated = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of("08:30"))
            .endTime(WorkScheduleTime.of("17:30"))
            .weekDay(WeekDay.TUESDAY)
            .sopRef(SopRef.of(sopRef))
            .build();

        WorkHourScheduleEntity entity = buildEntity(id, sopRef);
        WorkHourSchedule domain = buildSchedule(id, sopRef);

        Mockito.when(jpaRepo.findBySearchParams(id, null, null, null, null))
            .thenReturn(List.of(entity));
        Mockito.when(mapper.toDomain(entity)).thenReturn(domain);
        Mockito.when(jpaRepo.updateWorkHourSchedule(id, "08:30", "17:30", WeekDay.TUESDAY, sopRef))
            .thenReturn(1);

        repo.update(updated);

        Mockito.verify(jpaRepo).findBySearchParams(id, null, null, null, null);
        Mockito.verify(jpaRepo).updateWorkHourSchedule(id, "08:30", "17:30", WeekDay.TUESDAY, sopRef);
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }

    @Test
    void unhappyPath_update_shouldThrowWhenNotFound() {
        UUID id     = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        WorkHourSchedule updated = buildSchedule(id, sopRef);

        Mockito.when(jpaRepo.findBySearchParams(id, null, null, null, null))
            .thenReturn(List.of());

        ValidationException ex = assertThrows(ValidationException.class, () -> repo.update(updated));

        assertEquals("work_hour.not.found", ex.messageKey());
        Mockito.verify(jpaRepo).findBySearchParams(id, null, null, null, null);
        Mockito.verify(jpaRepo, Mockito.never()).updateWorkHourSchedule(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        );
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }

    @Test
    void unhappyPath_update_shouldThrowValidationExceptionOnInvalidData() {
        assertThrows(ValidationException.class, () ->
            WorkHourSchedule.builder()
                .id(WorkScheduleId.of(UUIDUtil.newUuid()))
                .startTime(WorkScheduleTime.of("invalid"))
                .endTime(WorkScheduleTime.of("17:30"))
                .weekDay(WeekDay.TUESDAY)
                .sopRef(SopRef.of(UUIDUtil.newUuid()))
                .build()
        );

        Mockito.verifyNoInteractions(jpaRepo);
        Mockito.verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------------------

    @Test
    void happyPath_delete_shouldBeIdempotent() {
        UUID id     = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        WorkHourSchedule schedule = buildSchedule(id, sopRef);
        WorkHourScheduleEntity entity = buildEntity(id, sopRef);

        Mockito.when(jpaRepo.findBySearchParams(id, null, null, null, null))
            .thenReturn(List.of(entity))
            .thenReturn(List.of());

        repo.delete(schedule);
        Mockito.verify(jpaRepo).delete(entity);

        assertDoesNotThrow(() -> repo.delete(schedule));
        Mockito.verify(jpaRepo).delete(entity); // still only called once
        Mockito.verify(jpaRepo, Mockito.times(2)).findBySearchParams(id, null, null, null, null);
        Mockito.verifyNoMoreInteractions(jpaRepo);
    }
}