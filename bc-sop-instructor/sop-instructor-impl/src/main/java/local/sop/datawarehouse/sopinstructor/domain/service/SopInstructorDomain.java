package local.sop.datawarehouse.sopinstructor.domain.service;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.sopinstructor.domain.model.SopInstructor;

public interface SopInstructorDomain {
    public SopInstructor createSopInstructor(CompositeKey id, Boolean active);
    public SopInstructor toggleActivateSopInstructor(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public SopInstructor deleteSopInstructor(CompositeKey id);
}
