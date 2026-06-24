package local.sop.sopinfo.message.saga.application.infrastructure.personnotification;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.PersonNotificationResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.request.PayloadPersonNotificationCreate;
import local.sop.sopinfo.message.saga.application.ports.out.personnotification.PersonNotificationPort;

@Component
public class PersonNotificationHttpAdapter implements PersonNotificationPort {

	private final RestClient personnotification;

	public PersonNotificationHttpAdapter(@Qualifier("personnotification") RestClient personnotification) {
		this.personnotification = personnotification;
	}

	@Override
	public void create(UUID personRef, UUID notificationRef) {
		personnotification.post()
			.uri("/internal/person-notifications")
			.body(new PayloadPersonNotificationCreate(personRef, notificationRef))
			.retrieve()
			.toBodilessEntity();
	}

	@Override
	public List<PersonNotificationResponse> findByNotificationRef(UUID notificationRef) {
		return personnotification.get()
			.uri("/internal/person-notifications?notificationRef={notificationRef}", notificationRef)
			.retrieve()
			.body(new ParameterizedTypeReference<List<PersonNotificationResponse>>() {});
	}
}
