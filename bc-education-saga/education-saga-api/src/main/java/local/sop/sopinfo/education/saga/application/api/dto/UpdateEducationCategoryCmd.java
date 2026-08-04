package local.sop.sopinfo.education.saga.application.api.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateEducationCategoryCmd(
    @NotNull(message = "{saga.sessionid.required}") UUID sessionId,
    @Size(max = 100, message = "education.category.size")
    @NotBlank(message = "education.category.required")
    @NotNull(message = "education.category.required")
    String category,
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
) {}
