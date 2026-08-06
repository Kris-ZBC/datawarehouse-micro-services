package local.sop.datawarehouse.messageperson.application.infrastructure.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    OffsetDateTime dateTimeSent,
    String message,
    UUID senderPersonRef
) {
}