package local.sop.datawarehouse.registration.saga.application.api.dto.person;

import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.PhoneUserType;


public record PhoneNumberResponse(
        UUID id,
        PhoneUserType type,
        String value
) {
}