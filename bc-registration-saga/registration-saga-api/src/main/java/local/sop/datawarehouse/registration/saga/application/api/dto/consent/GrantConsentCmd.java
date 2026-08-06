package local.sop.datawarehouse.registration.saga.application.api.dto.consent;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;

public record GrantConsentCmd(
        @NotNull(message="{consent.personref.invalid}") UUID personRef,
        @NotNull(message="{consent.consentstatementref.invalid}") UUID consentStatementRef,
        @NotNull(message = "{consent.purpose.invalid}") ConsentPurpose purpose,
        @NotNull(message = "{consent.type.invalid}") ConsentType type,
        @NotNull(message= "{consent.status.invalid}") ConsentStatus status
) {}