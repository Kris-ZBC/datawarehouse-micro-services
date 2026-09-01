package local.sop.datawarehouse.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;

// CHANGED: purpose and type removed. Both are properties of the
// ConsentStatement being agreed to, not something the granter supplies —
// they're resolved from the referenced statement, not passed in here.
public record GrantConsentCmd(
        @NotNull(message="{consent.personref.invalid}") UUID personRef,
        @NotNull(message="{consent.consentstatementref.invalid}") UUID consentStatementRef,
        @NotNull(message= "{consent.status.invalid}") ConsentStatus status
) {}