package local.sop.datawarehouse.personnotification.application.api.dto;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
public record CreatedPersonNotificationResult(
    CompositeKey id
) {
}
