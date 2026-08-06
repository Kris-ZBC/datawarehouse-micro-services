package local.sop.datawarehouse.apprentice.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;
import local.sop.datawarehouse.apprentice.application.api.dto.ApprenticeResponse;
import local.sop.datawarehouse.apprentice.application.api.dto.CreateApprenticeCmd;
import local.sop.datawarehouse.apprentice.application.api.dto.CreatedApprenticeResponse;

public interface ApprenticeDirectory extends Compensatable {
    
    CreatedApprenticeResponse createApprentice(CreateApprenticeCmd cmd);

    Optional<ApprenticeResponse> findById(UUID ApprenticeId);

    List<ApprenticeResponse> findByEducationLineId(UUID educationLineId);

    List<ApprenticeResponse> findAll();
}
