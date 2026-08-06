package local.sop.datawarehouse.educationline.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

public record PayloadAuditlogCreate(
    UUID actor, ActorType type, Severity severity, String originSystem, String originService,
            String originComponent, String data, String description
) {}
