package local.sop.sopinfo.workhour.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.workhour.application.api.dto.*;

public interface WorkHourDirectory {

    UUID create(CreateWorkHourScheduleCmd cmd);
	void update(UpdateWorkHourScheduleCmd cmd);
	Optional<WorkScheduleResponse> readById(FindByScheduleIdQuery query);
	List<WorkScheduleResponse> readByParams(FindByScheduleParamsQuery query);
	void delete(FindByScheduleIdQuery query);
}
 