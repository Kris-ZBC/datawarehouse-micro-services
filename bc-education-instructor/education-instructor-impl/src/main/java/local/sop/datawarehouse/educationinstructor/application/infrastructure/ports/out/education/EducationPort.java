package local.sop.datawarehouse.educationinstructor.application.infrastructure.ports.out.education;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.datawarehouse.educationinstructor.application.infrastructure.response.EducationResponse;

public interface EducationPort extends CompositeKeyValidator {
    Optional<EducationResponse> findById(UUID id);
}
