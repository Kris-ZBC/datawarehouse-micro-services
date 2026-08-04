package local.sop.sopinfo.instructor.application.api;

import java.util.List;
import java.util.UUID;

import local.sop.sopinfo.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.sopinfo.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.sopinfo.instructor.application.api.dto.InstructorResponse;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;

public interface InstructorDirectory extends Compensatable {

    CreatedInstructorResponse createInstructor(CreateInstructorCmd cmd);

    List<InstructorResponse> findAll();

    InstructorResponse findById(UUID id);
}