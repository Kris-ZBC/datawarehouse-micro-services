package local.sop.datawarehouse.registration.saga.application.ports.out.educationline;

import java.util.UUID;

import local.sop.datawarehouse.registration.saga.application.api.dto.educationline.EducationLineResponse;

public interface EducationLinePort {
	EducationLineResponse getById(UUID id);
}
