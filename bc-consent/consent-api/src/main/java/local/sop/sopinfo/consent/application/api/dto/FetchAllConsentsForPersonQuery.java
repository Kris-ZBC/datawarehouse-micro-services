package local.sop.sopinfo.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;


/** All consents registrered for this person */

public record FetchAllConsentsForPersonQuery(
        @NotNull(message="{consent.personref.invalid}") UUID personRef
) {}