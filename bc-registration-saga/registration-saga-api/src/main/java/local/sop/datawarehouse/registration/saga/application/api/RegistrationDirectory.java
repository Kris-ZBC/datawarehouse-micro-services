package local.sop.datawarehouse.registration.saga.application.api;

import local.sop.datawarehouse.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.CreatedApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreatedInstructorResponse;


public interface RegistrationDirectory {
    CreatedApprenticeResponse registerApprentice(CreateApprenticeRegistrationCmd cmd);
    CreatedInstructorResponse registerInstructor(CreateInstructorRegistrationCmd cmd);
}
