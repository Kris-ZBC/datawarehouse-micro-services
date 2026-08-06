package local.sop.datawarehouse.anonymize.saga.application.infrastructure.person;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.anonymize.saga.application.ports.out.person.PersonPort;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;

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
