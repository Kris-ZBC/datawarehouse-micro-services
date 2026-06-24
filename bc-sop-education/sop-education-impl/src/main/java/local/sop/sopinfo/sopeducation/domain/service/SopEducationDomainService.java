package local.sop.sopinfo.sopeducation.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;
import local.sop.sopinfo.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;

public class SopEducationDomainService implements SopEducationDomain {

    @Override
    public SopEducation createSopEducation(CompositeKey id, Boolean active) {
        /* Create a new SopEducationLine instance with the builder pattern */
        return new SopEducation.Builder()
                .id(id)
                .active(active)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();
    }

    @Override
    public SopEducation toggleActivateSopEducation(CompositeKey id, Boolean previousActive, LocalDateTime createdAt) {
        /* Implementation for toggling activation status */
        
        return new SopEducation.Builder()
                .id(id)
                .active(!previousActive)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();
    }

    @Override
    public SopEducation deleteSopEducation(CompositeKey id) {
        /* Implementation for deleting a SopEducationLine, which could be represented by setting active to false */
        
        return new SopEducation.Builder()
                .id(id)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();
    }

}
