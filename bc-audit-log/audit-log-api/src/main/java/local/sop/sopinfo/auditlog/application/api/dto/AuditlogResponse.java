package local.sop.sopinfo.auditlog.application.api.dto;

import java.time.Instant;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

public record AuditlogResponse(
        UUID id,
        UUID actorRef,
        ActorType actorType,
        Severity severity,
        String originSystem,
        String originService,
        String originComponent,
        String data,
        String description,
        Instant timestamp
) {}
