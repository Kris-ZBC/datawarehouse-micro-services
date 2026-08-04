package local.sop.sopinfo.message.saga.application.api.dto;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record MessagePersonResponse(
        CompositeKey id,
        Boolean active,
        LocalDateTime createdAt
) {}
