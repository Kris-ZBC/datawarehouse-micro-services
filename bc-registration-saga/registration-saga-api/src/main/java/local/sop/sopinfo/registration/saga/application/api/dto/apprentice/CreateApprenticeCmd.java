package local.sop.sopinfo.registration.saga.application.api.dto.apprentice;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateApprenticeCmd (
    @NotNull UUID personRef,
    @NotNull UUID educationLineRef
) {}
