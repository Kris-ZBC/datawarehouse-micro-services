package local.sop.sopinfo.message.saga.application.infrastructure.notification;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.NotificationResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.request.PayloadNotificationCreate;
import local.sop.sopinfo.message.saga.application.ports.out.notification.NotificationPort;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@Component
public class NotificationHttpAdapter implements NotificationPort {

	private final RestClient notification;

	public NotificationHttpAdapter(@Qualifier("notification") RestClient notification) {
		this.notification = notification;
	}

	@Override
	public UUID create(UUID messageRef) {
		NotificationResponse response = notification.post()
			.uri("/internal/notifications/create")
			.body(new PayloadNotificationCreate(messageRef))
			.retrieve()
			.body(NotificationResponse.class);
		if(response == null) {
			throw new RuntimeException("notification.empty.response");
		}
		return response.id();
	}

	@Override
	public NotificationResponse findById(UUID id) {
		NotificationResponse response = notification.get()
			.uri("/internal/notifications/{id}", id)
			.retrieve()
			.body(NotificationResponse.class);
		if(response == null) {
			throw new RuntimeException("notification.empty.response");
		}
		return response;
	}

	@Override
	public void deleteNotification(UUID id) {
		notification.delete()
			.uri("/internal/notifications/{id}", id)
			.retrieve()
			.toBodilessEntity();
	}

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		return this.notification.put()
			.uri("/internal/notifications/{id}/compensate/create", id)
			.body(new PayloadCompensateCreate(clazz, sagaState))
			.retrieve()
			.body(ResponseCompensated.class);
	}
}
