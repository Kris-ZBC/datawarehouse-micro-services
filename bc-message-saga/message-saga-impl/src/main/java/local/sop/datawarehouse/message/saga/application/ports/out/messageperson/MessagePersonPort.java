package local.sop.datawarehouse.message.saga.application.ports.out.messageperson;

import java.util.UUID;
import java.util.List;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.message.saga.application.api.dto.MessagePersonResponse;

public interface MessagePersonPort {
	CompositeKey create(UUID personRef, UUID messageRef);
	List<MessagePersonResponse> findByMessageRef(UUID messageRef);
}
