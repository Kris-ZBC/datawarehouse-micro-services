package local.sop.datawarehouse.messageperson.application.infrastructure.response;
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