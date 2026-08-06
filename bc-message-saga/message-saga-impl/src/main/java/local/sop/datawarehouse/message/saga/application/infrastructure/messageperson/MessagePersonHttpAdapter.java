package local.sop.datawarehouse.message.saga.application.infrastructure.messageperson;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.message.saga.application.api.dto.MessagePersonResponse;
import local.sop.datawarehouse.message.saga.application.infrastructure.request.PayloadMessagePersonCreate;
import local.sop.datawarehouse.message.saga.application.ports.out.messageperson.MessagePersonPort;

@Component
public class MessagePersonHttpAdapter implements MessagePersonPort {

		private final RestClient messageperson;

		public MessagePersonHttpAdapter(@Qualifier("messageperson") RestClient messageperson) {
			this.messageperson = messageperson;
		}

	@Override
	public CompositeKey create(UUID personRef, UUID messageRef) {
		MessagePersonResponse response = messageperson.post()
			.uri("/internal/message-persons")
			.body(new PayloadMessagePersonCreate(new CompositeKey(personRef, messageRef), true))
			.retrieve()
			.body(MessagePersonResponse.class);
		if(response == null) {
			throw new RuntimeException("messageperson.empty.response");
		}
		return response.id();
	}

	@Override
	public List<MessagePersonResponse> findByMessageRef(UUID messageRef) {
		return messageperson.get()
			.uri("/internal/message-persons/by-message/{messageId}", messageRef)
			.retrieve()
			.body(new ParameterizedTypeReference<List<MessagePersonResponse>>() {});
	}
}
