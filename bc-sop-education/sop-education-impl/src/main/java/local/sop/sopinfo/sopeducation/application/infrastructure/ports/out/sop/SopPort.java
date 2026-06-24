package local.sop.sopinfo.sopeducation.application.infrastructure.ports.out.sop;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.sopinfo.sopeducation.application.infrastructure.response.SopResponse;

public interface SopPort extends CompositeKeyValidator {
    Optional<SopResponse> findById(UUID id);
}
