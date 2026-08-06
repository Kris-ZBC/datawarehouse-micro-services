package local.sop.datawarehouse.educationinstructor.application.infrastructure.ports.out.instructor;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.datawarehouse.educationinstructor.application.infrastructure.response.InstructorResponse;

public interface InstructorPort extends CompositeKeyValidator {
    Optional<InstructorResponse> findById(UUID id);
}
