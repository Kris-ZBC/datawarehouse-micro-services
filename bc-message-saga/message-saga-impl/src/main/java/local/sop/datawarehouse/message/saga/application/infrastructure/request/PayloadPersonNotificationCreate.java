package local.sop.datawarehouse.message.saga.application.infrastructure.request;

import java.util.UUID;

public record PayloadPersonNotificationCreate(
	UUID personRef,
	UUID notificationRef
) {}
