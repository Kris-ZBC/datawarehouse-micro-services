package local.sop.datawarehouse.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;

public record FetchConsentForPersonAndPurposeQuery(
   @NotNull(message="{consent.personref.invalid}") UUID personId,
   @NotNull(message="{consent.purpose.invalid}") ConsentPurpose purpose
) {}