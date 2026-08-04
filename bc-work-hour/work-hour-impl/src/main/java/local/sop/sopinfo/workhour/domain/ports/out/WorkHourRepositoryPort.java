package local.sop.sopinfo.workhour.domain.ports.out;


import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.workhour.domain.model.WorkHour;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkHourId;

public interface WorkHourRepositoryPort {
   WorkHour save(WorkHour workHour);
   WorkHour update(WorkHour workHour);
   Optional<WorkHour> findById(WorkHourId id);
   List<WorkHour> getAll();
}
