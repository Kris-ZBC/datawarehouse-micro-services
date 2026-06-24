package local.sop.sopinfo.anonymize.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;

public record PayloadAuditlogCreate(
    UUID actor, ActorType type, Severity severity, String originSystem, String originService,
            String originComponent, String data, String description
) {}
