package local.sop.sopinfo.sopeducation.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;

public interface SopEducationDomain {
    public SopEducation createSopEducation(CompositeKey id, Boolean active);
    public SopEducation toggleActivateSopEducation(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public SopEducation deleteSopEducation(CompositeKey id);
}
