package local.sop.datawarehouse.consent.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;

public record PayloadAuditlogCreate(
    UUID actor, ActorType type, Severity severity, String originSystem, String originService,
            String originComponent, String data, String description
) {}
