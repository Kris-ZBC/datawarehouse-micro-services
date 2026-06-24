package local.sop.sopinfo.education.saga.application.infrastructure.request;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record PayloadCreateEducationInstructor(
    CompositeKey id
) {
}
