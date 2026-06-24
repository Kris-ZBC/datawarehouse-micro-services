package local.sop.sopinfo.message.saga.application.infrastructure.request;

import java.util.UUID;

public record PayloadNotificationCreate(
	UUID messageRef
) {}
