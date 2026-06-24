package local.sop.sopinfo.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RevokeConsentCmd(
        @NotNull(message="{consent.consentid.invalid}") UUID consentId
) {
}
