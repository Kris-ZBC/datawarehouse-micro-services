package local.sop.datawarehouse.registration.saga.application.api.dto.instructor;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateInstructorCmd(
        @NotNull UUID personRef
) {}