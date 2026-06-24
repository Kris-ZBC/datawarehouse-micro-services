package local.sop.sopinfo.sopinstructor.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public class SopInstructorDomainService implements SopInstructorDomain{

    @Override
    public SopInstructor createSopInstructor(CompositeKey id, Boolean active) {
        /* Create a new SopInstructor instance with the builder pattern */
        return new  SopInstructor.Builder()
            .id(id)
            .active(active)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
            .build();
    }

    @Override
    public SopInstructor toggleActivateSopInstructor(CompositeKey id, Boolean previousActive, LocalDateTime createdAt) {
        /* Implementation for toggling activation status */
        return new SopInstructor.Builder()
            .id(id)
            .active(!previousActive)
            .createdAt(new CreatedAtTimestamp(createdAt))
            .build();
    }

    @Override
    public SopInstructor deleteSopInstructor(CompositeKey id) {
        /* Implementation for deleting a SopInstructor, which could be repreented by setting active to false */
        return new SopInstructor.Builder()
            .id(id)
            .active(false)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
            .build();
    }

}
