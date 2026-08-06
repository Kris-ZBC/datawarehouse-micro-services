package local.sop.datawarehouse.sopinstructor.application.infrastructure.ports.out.sop;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.datawarehouse.sopinstructor.application.infrastructure.response.SopResponse;

public interface SopPort extends CompositeKeyValidator {
    Optional<SopResponse> findById(UUID id);
}
