package local.sop.datawarehouse.message.application.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    OffsetDateTime dateTimeSent,
    String message,
    UUID senderPersonRef
) {
}