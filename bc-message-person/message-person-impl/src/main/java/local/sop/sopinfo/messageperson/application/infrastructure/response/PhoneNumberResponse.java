package local.sop.sopinfo.messageperson.application.infrastructure.response;
import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;

public record PhoneNumberResponse(
        UUID id,
        PhoneUserType type,
        String value
) {
}