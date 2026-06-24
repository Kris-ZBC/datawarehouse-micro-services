package local.sop.sopinfo.workhour.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;

public interface WorkHourScheduleRepositoryPort {
    WorkHourSchedule save(WorkHourSchedule s);
    Optional<WorkHourSchedule> findById(UUID id);
    List<WorkHourSchedule> findBySearchParams(UUID id, String startTime, String endTime, String weekDay, UUID sopRef);
    void update(WorkHourSchedule s);
    void delete(WorkHourSchedule s);
}
