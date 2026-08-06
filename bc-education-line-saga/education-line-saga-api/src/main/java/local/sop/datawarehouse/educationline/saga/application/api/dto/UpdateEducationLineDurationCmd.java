package local.sop.datawarehouse.educationline.saga.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;

public record UpdateEducationLineDurationCmd(
	@Min(value = 0, message = "{educationline.duration.years.min}")
	@NotNull(message = "{educationline.duration.years.required}") Integer durationYears,
	@Min(value = 0, message = "{educationline.duration.months.min}")
	@Max(value = 11, message = "{educationline.duration.months.max}")
	@NotNull(message = "{educationline.duration.months.required}") Integer durationMonths,
	@Min(value = 0, message = "{educationline.duration.days.min}")
	@Max(value = 30, message = "{educationline.duration.days.max}")
	@NotNull(message = "{educationline.duration.days.required}") Integer durationDays,
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
