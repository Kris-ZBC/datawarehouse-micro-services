package local.sop.sopinfo.message.saga.application.infrastructure.request;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record PayloadMessagePersonCreate(
	CompositeKey id,
	Boolean active
) {}
