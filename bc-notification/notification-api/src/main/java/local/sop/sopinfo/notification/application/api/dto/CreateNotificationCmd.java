package local.sop.sopinfo.notification.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateNotificationCmd(
	@NotNull(message = "{messageref.invalid}") UUID messageRef
) {} 
