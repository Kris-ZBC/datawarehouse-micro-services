package local.sop.datawarehouse.sopeducation.application.infrastructure.response;

import java.util.UUID;

public record SopResponse(UUID id,
    String name,
    String address,
    String education) {

}
