package local.sop.datawarehouse.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record FetchConsentStatementQuery(
   @NotNull(message="{key.invalid}") UUID consentStatementId
) {}