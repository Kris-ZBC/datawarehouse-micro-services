package local.sop.datawarehouse.message.saga.application.infrastructure.request;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record PayloadMessagePersonCreate(
	CompositeKey id,
	Boolean active
) {}
