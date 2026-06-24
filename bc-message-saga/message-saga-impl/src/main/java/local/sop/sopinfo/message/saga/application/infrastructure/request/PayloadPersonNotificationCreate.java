package local.sop.sopinfo.message.saga.application.infrastructure.request;

import java.util.UUID;

public record PayloadPersonNotificationCreate(
	UUID personRef,
	UUID notificationRef
) {}
