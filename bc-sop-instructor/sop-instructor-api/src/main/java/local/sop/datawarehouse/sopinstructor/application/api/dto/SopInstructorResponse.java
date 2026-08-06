package local.sop.datawarehouse.sopinstructor.application.api.dto;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record SopInstructorResponse(
    CompositeKey id,
    Boolean active,
    LocalDateTime createdAt
) {

}
