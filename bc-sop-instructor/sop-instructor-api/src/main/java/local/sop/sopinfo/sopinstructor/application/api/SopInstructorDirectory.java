package local.sop.sopinfo.sopinstructor.application.api;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.sopinstructor.application.api.dto.CreateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreatedSopInstructorResult;
import local.sop.sopinfo.sopinstructor.application.api.dto.SopInstructorResponse;
import local.sop.sopinfo.sopinstructor.application.api.dto.ToggleActivateSopInstructorCmd;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public interface SopInstructorDirectory {
    CreatedSopInstructorResult create(CreateSopInstructorCmd command);
    Optional<SopInstructorResponse> findById(CompositeKey id);
    SopInstructorResponse toggleActive(ToggleActivateSopInstructorCmd command);
    List<SopInstructorResponse> getBySopRef(java.util.UUID sopRef);
    List<SopInstructorResponse> getByInstructorRef(java.util.UUID instructorRef);
}
