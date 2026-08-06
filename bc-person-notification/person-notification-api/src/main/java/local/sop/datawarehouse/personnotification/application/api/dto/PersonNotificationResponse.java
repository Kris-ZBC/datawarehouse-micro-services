package local.sop.datawarehouse.personnotification.application.api.dto;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;


	public record PersonNotificationResponse(
    CompositeKey id,
    Boolean active,
    LocalDateTime createdAt
){
	
}
