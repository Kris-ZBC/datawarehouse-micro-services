package local.sop.sopinfo.message.saga.application.api;

import local.sop.sopinfo.message.saga.application.api.dto.CreateMessageCmd;
import local.sop.sopinfo.message.saga.application.api.dto.MessageResponse;
public interface MessageSagaDirectory {
	MessageResponse createMessage(CreateMessageCmd cmd);
}
