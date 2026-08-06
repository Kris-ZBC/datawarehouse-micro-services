package local.sop.datawarehouse.message.saga.application.api.dto;

import java.util.UUID;

public record PersonResponse(
	UUID id,
	String firstName,
	String lastName,
	String email,
	UUID organizationRef
) {}
