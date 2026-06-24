package local.sop.sopinfo.registration.saga.application.api.dto.person;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;

public record PhoneNumberResponse(
        UUID id,
        PhoneUserType type,
        String value
) {
}