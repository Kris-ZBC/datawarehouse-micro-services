package local.sop.sopinfo.sopeducation.application.api.dto;

import java.time.LocalDateTime;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record SopEducationResponse(
    CompositeKey id,
    Boolean active,
    LocalDateTime createdAt
) {

}
