package local.sop.datawarehouse.sopeducation.application.api.dto;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record SopEducationResponse(
    CompositeKey id,
    Boolean active,
    LocalDateTime createdAt
) {

}
