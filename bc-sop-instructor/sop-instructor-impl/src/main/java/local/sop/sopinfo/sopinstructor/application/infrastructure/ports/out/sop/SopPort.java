package local.sop.sopinfo.sopinstructor.application.infrastructure.ports.out.sop;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.sopinstructor.application.infrastructure.response.SopResponse;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface SopPort extends CompositeKeyValidator {
    Optional<SopResponse> findById(UUID id);
}
