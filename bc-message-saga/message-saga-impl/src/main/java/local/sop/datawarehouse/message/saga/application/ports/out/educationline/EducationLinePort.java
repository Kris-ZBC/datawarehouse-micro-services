package local.sop.datawarehouse.message.saga.application.ports.out.educationline;

import java.util.List;
import java.util.UUID;

import local.sop.datawarehouse.message.saga.application.api.dto.EducationLineResponse;

public interface EducationLinePort {
	List<EducationLineResponse> findByEducationRef(UUID educationRef);
}
