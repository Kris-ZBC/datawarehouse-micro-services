package local.sop.sopinfo.message.saga.application.ports.out.apprentice;

import java.util.List;
import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.ApprenticeResponse;

public interface ApprenticePort {
	List<ApprenticeResponse> findByEducationLineRef(UUID educationLineRef);
}
