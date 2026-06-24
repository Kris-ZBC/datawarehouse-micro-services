package local.sop.sopinfo.message.saga.application.infrastructure.person;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.PersonResponse;
import local.sop.sopinfo.message.saga.application.ports.out.person.PersonPort;

@Component
public class PersonHttpAdapter implements PersonPort {

	private final RestClient person;

	public PersonHttpAdapter(@Qualifier("person") RestClient person) {
		this.person = person;
	}

	@Override
	public PersonResponse findById(UUID id) {
		PersonResponse response = person.get()
			.uri("/internal/persons/{id}", id)
			.retrieve()
			.body(PersonResponse.class);
		if(response == null) {
			throw new RuntimeException("person.empty.response");
		}
		return response;
	}
}
