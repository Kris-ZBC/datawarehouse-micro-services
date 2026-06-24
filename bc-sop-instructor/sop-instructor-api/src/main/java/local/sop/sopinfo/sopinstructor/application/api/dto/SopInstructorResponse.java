package local.sop.sopinfo.sopinstructor.application.api.dto;

import java.time.LocalDateTime;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record SopInstructorResponse(
    CompositeKey id,
    Boolean active,
    LocalDateTime createdAt
) {

}
