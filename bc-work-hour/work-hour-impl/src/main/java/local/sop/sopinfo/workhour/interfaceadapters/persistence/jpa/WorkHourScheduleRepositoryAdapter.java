package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.ports.out.WorkHourScheduleRepositoryPort;

@Repository
public class WorkHourScheduleRepositoryAdapter implements WorkHourScheduleRepositoryPort {
    private static final Logger log = LoggerFactory.getLogger(WorkHourScheduleRepositoryAdapter.class);
    private final WorkHourScheduleMapper mapper;
    private final WorkHourScheduleSpringDataRepository repo;

    public WorkHourScheduleRepositoryAdapter(WorkHourScheduleMapper mapper, WorkHourScheduleSpringDataRepository repo) {
        this.mapper = mapper;
        this.repo = repo;

    }

    @Override
    public WorkHourSchedule save(WorkHourSchedule s) {
        WorkHourScheduleEntity entity = mapper.toEntity(s);
        entity = repo.save(entity);
        entity = repo.findById(entity.getId()).orElseThrow();
        log.info("saved record with key {}", entity.getId().toString());
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<WorkHourSchedule> findById(UUID id) {
        return Optional.ofNullable(findBySearchParams(id, null, null, null, null)
        .stream()
        .filter(a -> a.getId().value().equals(id)).findFirst()
        .orElse(null));
    }

    @Override
    public List<WorkHourSchedule> findBySearchParams(UUID id, String startTime, String endTime, String weekDay, UUID sopRef) {
        List<WorkHourScheduleEntity> entities = repo.findBySearchParams(
            id, startTime, endTime, weekDay != null? WeekDay.parse(weekDay): null, sopRef);
        return entities.stream().map(mapper::toDomain).toList();        
    }

        
    @Override
    public void update(WorkHourSchedule s) {
        WorkHourSchedule existing = findById(s.getId().value())
            .orElseThrow(() -> new ValidationException("work_hour.not.found", Map.of("method", "update")));
        
        log.info("Updating existing schedule: {}", existing.getId().value());
        
        int updatedRecords = repo.updateWorkHourSchedule(
            s.getId().value(), 
            s.getStartTime().time(), 
            s.getEndTime().time(), 
            s.getWeekDay(), 
            s.getSopRef().value()
        );
        
        log.info("updated {} records", updatedRecords);

    }

    @Override
    public void delete(WorkHourSchedule s) {
        List<WorkHourScheduleEntity> entities = repo.findBySearchParams(
        s.getId().value(), null, null, null, null
        );
        
        if (!entities.isEmpty()) {
            repo.delete(entities.get(0));
            log.info("deleted record for key {}", s.getId().value().toString());
        }
    }

}
