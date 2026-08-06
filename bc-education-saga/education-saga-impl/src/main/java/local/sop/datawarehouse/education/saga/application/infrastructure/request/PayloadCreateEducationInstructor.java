package local.sop.datawarehouse.education.saga.application.infrastructure.request;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record PayloadCreateEducationInstructor(
    CompositeKey id
) {
}
