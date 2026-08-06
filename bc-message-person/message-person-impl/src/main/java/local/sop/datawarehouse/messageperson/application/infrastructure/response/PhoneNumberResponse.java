package local.sop.datawarehouse.messageperson.application.infrastructure.response;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;

public record PhoneNumberResponse(
        UUID id,
        PhoneUserType type,
        String value
) {
}