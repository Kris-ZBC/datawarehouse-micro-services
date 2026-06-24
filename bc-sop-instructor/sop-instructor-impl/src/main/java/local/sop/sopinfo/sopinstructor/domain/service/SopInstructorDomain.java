package local.sop.sopinfo.sopinstructor.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public interface SopInstructorDomain {
    public SopInstructor createSopInstructor(CompositeKey id, Boolean active);
    public SopInstructor toggleActivateSopInstructor(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public SopInstructor deleteSopInstructor(CompositeKey id);
}
