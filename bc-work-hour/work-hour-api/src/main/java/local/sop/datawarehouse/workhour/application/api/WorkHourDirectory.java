package local.sop.datawarehouse.workhour.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.datawarehouse.workhour.application.api.dto.*;

public interface WorkHourDirectory {

    CreatedWorkHourResult create(CreateWorkHourCmd cmd);
	WorkHourResponse update(UpdateWorkHourCmd cmd);
	Optional<WorkHourResponse> findById(UUID id);
	List<WorkHourResponse> getAll();
}
 