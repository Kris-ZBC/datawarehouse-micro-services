package local.sop.datawarehouse.consent.saga.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.enums.Severity;

// CHANGED: purpose/type added — mirrors bc-consent's own
// CreateConsentStatementCmd, which now requires both since they're
// properties of the statement itself (see bc-consent's domain model
// change: purpose/type moved from Consent to ConsentStatement).
public record CreateConsentStatementCmd(
    @NotNull(message = "{saga.sessionid.required}") UUID sessionId,
    @NotNull(message= "{consentstatement.active.required}") Boolean active,
    @NotNull(message= "{consent.statementtext.invalid}")
    @NotBlank(message= "{consent.statementtext.invalid}") 
    @Size(max=1000, message= "{consent.statementtext.length.invalid}")
    String statementText,
    @NotNull(message = "{consentstatement.purpose.invalid}") ConsentPurpose purpose,
    @NotNull(message = "{consentstatement.type.invalid}") ConsentType type,
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
