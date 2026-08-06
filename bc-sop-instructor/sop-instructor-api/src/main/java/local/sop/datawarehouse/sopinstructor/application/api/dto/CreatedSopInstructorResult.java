package local.sop.datawarehouse.sopinstructor.application.api.dto;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record CreatedSopInstructorResult(
    CompositeKey id
) {

}
