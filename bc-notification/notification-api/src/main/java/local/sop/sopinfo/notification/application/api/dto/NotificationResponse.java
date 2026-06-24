package local.sop.sopinfo.notification.application.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
	UUID id,
	boolean seen,
	LocalDateTime createdAt,
	UUID messageRef
) {} 
