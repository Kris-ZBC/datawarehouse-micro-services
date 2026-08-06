package local.sop.datawarehouse.educationinstructor.domain.service;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.educationinstructor.domain.model.EducationInstructor;
import local.sop.datawarehouse.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;

public class EducationInstructorDomainService implements EducationInstructorDomain {

    @Override
    public EducationInstructor createEducationInstructor(CompositeKey id, Boolean active) {
        return EducationInstructor.builder()
                .id(id)
                .active(active)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();
    }

    @Override
    public EducationInstructor toggleActivateEducationInstructor(
            CompositeKey id,
            Boolean previousActive,
            LocalDateTime createdAt) {

        return EducationInstructor.builder()
                .id(id)
                .active(!previousActive)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();
    }

    @Override
    public EducationInstructor deleteEducationInstructor(CompositeKey id) {

        return EducationInstructor.builder()
                .id(id)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();
    }
}