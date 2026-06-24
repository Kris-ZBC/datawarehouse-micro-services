package local.sop.sopinfo.personnotification.application.infrastructure.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID messageRef,
    LocalDateTime createdAt,
    boolean seen
) {

}

