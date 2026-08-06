package local.sop.datawarehouse.registration.saga.application.api.dto.person;

import java.util.List;
import java.util.UUID;

public record PersonResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        UUID organizationRef,
        List<PhoneNumberResponse> phoneNumbers
) {
}