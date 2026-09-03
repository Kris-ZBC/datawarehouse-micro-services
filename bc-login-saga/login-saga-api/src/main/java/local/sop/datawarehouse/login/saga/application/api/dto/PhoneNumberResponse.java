package local.sop.datawarehouse.login.saga.application.api.dto;
import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.PhoneUserType;


public record PhoneNumberResponse(
        UUID id,
        PhoneUserType type,
        String value
) {
}