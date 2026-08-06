package local.sop.datawarehouse.message.saga.application.ports.out.educationinstructor;

import java.util.List;
import java.util.UUID;

import local.sop.datawarehouse.message.saga.application.api.dto.EducationInstructorResponse;

public interface EducationInstructorPort {
	List<EducationInstructorResponse> findByEducationRef(UUID educationRef);
}
