package local.sop.sopinfo.education.saga.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.enums.ActorType;

public record CreateEducationInstructorCmd(
    @NotNull(message = "{saga.sessionid.required}") UUID sessionId,
    @NotNull(message = "educationInstructor.id.required")
    CompositeKey id,
    @NotNull(message= "{log.actorref.required}") UUID educationRef,
    @NotNull(message= "{log.actorref.required}") UUID instructorRef,
    @NotNull(message= "{log.actorref.required}") UUID actorRef,
    @NotNull(message= "{log.actortype.required}") ActorType actorType,
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
) { }