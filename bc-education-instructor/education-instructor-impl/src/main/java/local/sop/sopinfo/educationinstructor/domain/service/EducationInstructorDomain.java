package local.sop.sopinfo.educationinstructor.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public interface EducationInstructorDomain {
	public EducationInstructor createEducationInstructor(CompositeKey id, Boolean active);
	public EducationInstructor toggleActivateEducationInstructor(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
	public EducationInstructor deleteEducationInstructor(CompositeKey id);
}
