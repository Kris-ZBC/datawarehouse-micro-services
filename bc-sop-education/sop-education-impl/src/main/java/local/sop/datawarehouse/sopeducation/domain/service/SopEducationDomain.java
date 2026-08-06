package local.sop.datawarehouse.sopeducation.domain.service;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.sopeducation.domain.model.SopEducation;

public interface SopEducationDomain {
    public SopEducation createSopEducation(CompositeKey id, Boolean active);
    public SopEducation toggleActivateSopEducation(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public SopEducation deleteSopEducation(CompositeKey id);
}
