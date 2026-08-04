package local.sop.sopinfo.educationinstructor.application.infrastructure.ports.out.education;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.educationinstructor.application.infrastructure.response.EducationResponse;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface EducationPort extends CompositeKeyValidator {
    Optional<EducationResponse> findById(UUID id);
}
