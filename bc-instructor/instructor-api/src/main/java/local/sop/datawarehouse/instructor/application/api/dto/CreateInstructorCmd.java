package local.sop.datawarehouse.instructor.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateInstructorCmd(
        @NotNull UUID personRef
) {}