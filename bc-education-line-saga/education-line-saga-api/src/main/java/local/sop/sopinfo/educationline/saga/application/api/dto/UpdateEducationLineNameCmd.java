package local.sop.sopinfo.educationline.saga.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

public record UpdateEducationLineNameCmd(
	@NotNull(message = "{educationline.name.required}") 
	@NotBlank(message = "{educationline.name.required}")
	@Size(max = 100, message = "{educationline.name.size}")
	String name,
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
