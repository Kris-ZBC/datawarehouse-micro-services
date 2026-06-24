package local.sop.sopinfo.personnotification.application.api.dto;

import java.time.LocalDateTime;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;


	public record PersonNotificationResponse(
    CompositeKey id,
    Boolean active,
    LocalDateTime createdAt
){
	
}
