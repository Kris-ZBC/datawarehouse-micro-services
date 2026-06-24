package local.sop.sopinfo.message.saga.application.api.dto;

import java.util.UUID;

public record PersonNotificationResponse(
        UUID id,
        UUID personRef,
        UUID notificationRef
) {}
