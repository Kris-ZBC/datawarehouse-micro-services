package local.sop.sopinfo.message.saga.application.infrastructure.message;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.ports.out.message.MessagePort;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.message.saga.application.api.dto.MessageResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.request.PayloadMessageCreate;

@Component
public class MessageHttpAdapter implements MessagePort {

	private final RestClient message;

	public MessageHttpAdapter(@Qualifier("message") RestClient message) {
		this.message = message;
	}

	@Override
	public MessageResponse create(UUID senderPersonRef, String message) {
		MessageResponse response = this.message.post()
			.uri("/internal/messages")
			.body(new PayloadMessageCreate(senderPersonRef, message))
			.retrieve()
			.body(MessageResponse.class);
		if(response == null) {
			throw new RuntimeException("message.empty.response");
		}
		return response;
	}

	@Override
	public MessageResponse findById(UUID id) {
		MessageResponse response = this.message.get()
			.uri("/internal/messages/{id}", id)
			.retrieve()
			.body(MessageResponse.class);
		if(response == null) {
			throw new RuntimeException("message.empty.response");
		}
		return response;
	}

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		return this.message.put()
			.uri("/internal/messages/{id}/compensate/create", id)
			.body(new PayloadCompensateCreate(clazz, sagaState))
			.retrieve()
			.body(ResponseCompensated.class);
	}
}
