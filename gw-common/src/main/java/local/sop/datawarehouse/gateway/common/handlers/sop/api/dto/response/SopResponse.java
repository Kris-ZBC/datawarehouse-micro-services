package local.sop.datawarehouse.gateway.common.handlers.sop.api.dto.response;

import java.util.List;
import java.util.UUID;

public record SopResponse(
    List<Sop> sops
) {
    public record Sop(
        UUID id,
        String name,
        String address,
        String education
    ) {}

}
