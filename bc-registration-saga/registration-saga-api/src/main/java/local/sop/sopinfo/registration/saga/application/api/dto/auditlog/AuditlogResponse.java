package local.sop.sopinfo.registration.saga.application.api.dto.auditlog;

import java.time.Instant;
import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;

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
