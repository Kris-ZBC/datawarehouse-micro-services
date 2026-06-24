package local.sop.sopinfo.apprentice.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateApprenticeCmd (
    @NotNull UUID personRef,
    @NotNull UUID educationLineRef
) {}
