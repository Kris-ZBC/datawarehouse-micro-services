package local.sop.sopinfo.person.application.api.dto;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;

public record PhoneNumberResponse(
        UUID id,
        PhoneUserType type,
        String value
) {
}