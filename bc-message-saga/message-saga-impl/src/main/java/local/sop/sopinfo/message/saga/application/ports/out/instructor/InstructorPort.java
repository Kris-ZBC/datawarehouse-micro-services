package local.sop.sopinfo.message.saga.application.ports.out.instructor;

import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.InstructorResponse;

public interface InstructorPort {
	InstructorResponse findById(UUID id);
}
