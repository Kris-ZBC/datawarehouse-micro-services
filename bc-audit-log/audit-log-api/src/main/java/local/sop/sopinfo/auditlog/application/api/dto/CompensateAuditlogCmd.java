package local.sop.sopinfo.auditlog.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensateAuditlogCmd(
    @NotNull
    Class<?> clazz,
    @NotNull
    SagaOutcome sagaState

) {}
