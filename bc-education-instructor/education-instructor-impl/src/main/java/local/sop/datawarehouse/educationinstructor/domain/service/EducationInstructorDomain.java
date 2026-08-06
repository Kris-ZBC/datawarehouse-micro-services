package local.sop.datawarehouse.educationinstructor.domain.service;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.educationinstructor.domain.model.EducationInstructor;

public interface EducationInstructorDomain {
	public EducationInstructor createEducationInstructor(CompositeKey id, Boolean active);
	public EducationInstructor toggleActivateEducationInstructor(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
	public EducationInstructor deleteEducationInstructor(CompositeKey id);
}
