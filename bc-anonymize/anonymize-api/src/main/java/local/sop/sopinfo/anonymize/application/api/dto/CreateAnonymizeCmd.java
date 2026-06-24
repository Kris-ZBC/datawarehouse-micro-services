package local.sop.sopinfo.anonymize.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateAnonymizeCmd(
        @NotNull UUID personRef
) {}