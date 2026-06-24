package local.sop.sopinfo.messageperson.application.infrastructure.ports.out.message;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.messageperson.application.infrastructure.response.MessageResponse;
import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface MessagePort extends CompositeKeyValidator {
    Optional<MessageResponse> findById(UUID id);
}
