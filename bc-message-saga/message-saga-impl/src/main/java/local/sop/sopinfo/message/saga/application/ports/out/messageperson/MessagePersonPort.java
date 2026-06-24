package local.sop.sopinfo.message.saga.application.ports.out.messageperson;

import java.util.UUID;
import java.util.List;

import local.sop.sopinfo.message.saga.application.api.dto.MessagePersonResponse;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public interface MessagePersonPort {
	CompositeKey create(UUID personRef, UUID messageRef);
	List<MessagePersonResponse> findByMessageRef(UUID messageRef);
}
