package local.sop.datawarehouse.messageperson.application.infrastructure.ports.out.message;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.datawarehouse.messageperson.application.infrastructure.response.MessageResponse;

public interface MessagePort extends CompositeKeyValidator {
    Optional<MessageResponse> findById(UUID id);
}
