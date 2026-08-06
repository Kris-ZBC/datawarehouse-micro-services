package local.sop.datawarehouse.registration.saga.application.api.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;

public record CreateApprenticeRegistrationCmd(
    String firstName,
    String lastName,
    String email,
    UUID organizationRef,
    List<@Valid CreatePhoneNumberCmd> phoneNumbers,
    UUID educationLineRef,
    String username,
    String status,
    UUID consentStatementId, 
    ConsentPurpose purpose, 
    ConsentType type, 
    ConsentStatus consentStatus
) {
}