package local.sop.datawarehouse.registration.saga.application.api.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;

public record CreateInstructorRegistrationCmd(
    String firstName,
    String lastName,
    String email,
    UUID organizationRef,
    List<@Valid CreatePhoneNumberCmd> phoneNumbers,
    String username,
    String status,
    List<@Valid ConsentStatement> consentStatements,
    UUID callerLoginId
    
) {
    public record ConsentStatement(UUID consentStatementRef, ConsentStatus status) {}
}
