package local.sop.sopinfo.consent.saga.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.ConsentPurpose;
import local.sop.sopinfo.sharedkernel.enums.ConsentStatus;
import local.sop.sopinfo.sharedkernel.enums.ConsentType;
import local.sop.sopinfo.sharedkernel.enums.Severity;

public record GrantConsentCmd(
        @NotNull(message = "{saga.sessionid.required}") UUID sessionId,
        @NotNull(message="{consent.personref.invalid}") UUID personRef,
        @NotNull(message="{consent.consentstatementref.invalid}") UUID consentStatementRef,
        @NotNull(message = "{consent.purpose.invalid}") ConsentPurpose purpose,
        @NotNull(message = "{consent.type.invalid}") ConsentType type,
        @NotNull(message= "{consent.status.invalid}") ConsentStatus status,
        @NotNull(message= "{log.actorref.required}") UUID actorRef,
        @NotNull(message= " {log.actortype.required}") ActorType actorType,
        @NotNull(message= "{log.severity.required}") Severity severity,
        @NotBlank(message= "{log.origin.system.required}") @NotNull String originSystem,
        @NotBlank(message= "{log.origin.service.required}") @NotNull String originService,
        @NotBlank(message = "{log.origin.component.required}") 
        @NotNull(message = "{log.origin.component.required}") 
        String originComponent,
        @NotBlank(message = "{log.data.required}")  
        @NotNull(message = "{log.data.required}")  
        String data,
        @NotBlank(message = "{log.description.required}")  
        @NotNull(message = "{log.description.required}") 
        String description
) {}