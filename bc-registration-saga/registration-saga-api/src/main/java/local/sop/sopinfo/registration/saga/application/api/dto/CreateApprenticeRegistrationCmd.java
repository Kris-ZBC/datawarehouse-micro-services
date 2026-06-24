package local.sop.sopinfo.registration.saga.application.api.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import local.sop.sopinfo.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;
import local.sop.sopinfo.sharedkernel.enums.ConsentPurpose;
import local.sop.sopinfo.sharedkernel.enums.ConsentStatus;
import local.sop.sopinfo.sharedkernel.enums.ConsentType;

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