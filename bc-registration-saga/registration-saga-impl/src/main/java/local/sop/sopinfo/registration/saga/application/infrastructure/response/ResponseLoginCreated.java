package local.sop.sopinfo.registration.saga.application.infrastructure.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseLoginCreated(
	UUID id,
	String password,
	LocalDateTime createdAt
) { }
