package local.sop.sopinfo.sopinstructor.application.infrastructure.ports.out.instructor;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.sopinstructor.application.infrastructure.response.InstructorResponse;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface InstructorPort extends CompositeKeyValidator {
    Optional<InstructorResponse> findById(UUID id);
}
