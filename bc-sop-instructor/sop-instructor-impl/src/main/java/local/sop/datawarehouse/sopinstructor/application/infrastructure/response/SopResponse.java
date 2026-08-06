package local.sop.datawarehouse.sopinstructor.application.infrastructure.response;

import java.util.UUID;

public record SopResponse(UUID id,
    String name,
    String address,
    String education) {
}
