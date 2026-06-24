package local.sop.sopinfo.anonymize.saga.application.infrastructure.person;


import local.sop.sopinfo.person.application.api.dto.PersonResponse;
import local.sop.sopinfo.anonymize.saga.application.ports.out.person.PersonPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class PersonHttpAdapter implements PersonPort {

	private final RestClient person;

	public PersonHttpAdapter(@Qualifier("person") RestClient person) {
		this.person = person;
	}

	@Override
	public PersonResponse get(UUID personRef) {
		return person.get()
			.uri("/internal/persons/{personRef}", personRef)
			.retrieve()
			.body(PersonResponse.class);
	}

}
