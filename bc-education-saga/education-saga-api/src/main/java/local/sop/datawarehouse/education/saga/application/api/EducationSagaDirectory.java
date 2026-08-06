package local.sop.datawarehouse.education.saga.application.api;

import java.util.UUID;

import local.sop.datawarehouse.education.saga.application.api.dto.*;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public interface EducationSagaDirectory {
    
    EducationResponse createEducation(CreateEducationCmd cmd);
    EducationResponse updateEducationName(UUID educationId, UpdateEducationNameCmd cmd);
    EducationResponse updateEducationCategory(UUID educationId, UpdateEducationCategoryCmd cmd);
    EducationResponse activateEducation(UUID educationId, CreateAuditlog auditlog);
    EducationResponse deactivateEducation(UUID educationId, CreateAuditlog auditlog);
    
    EducationInstructorResponse createEducationInstructor(CreateEducationInstructorCmd cmd);
    EducationInstructorResponse activateEducationInstructor(CompositeKey id, CreateAuditlog auditlog);
    EducationInstructorResponse deactivateEducationInstructor(CompositeKey id, CreateAuditlog auditlog);
}
