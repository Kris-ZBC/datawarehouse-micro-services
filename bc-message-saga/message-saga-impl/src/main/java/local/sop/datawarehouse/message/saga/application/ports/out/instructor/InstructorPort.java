package local.sop.datawarehouse.message.saga.application.ports.out.instructor;

import java.util.UUID;

import local.sop.datawarehouse.message.saga.application.api.dto.InstructorResponse;

public interface InstructorPort {
	InstructorResponse findById(UUID id);
}
