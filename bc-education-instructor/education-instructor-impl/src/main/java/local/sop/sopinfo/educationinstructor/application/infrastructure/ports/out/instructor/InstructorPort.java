package local.sop.sopinfo.educationinstructor.application.infrastructure.ports.out.instructor;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.educationinstructor.application.infrastructure.response.InstructorResponse;
import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface InstructorPort extends CompositeKeyValidator {
    Optional<InstructorResponse> findById(UUID id);
}
