package local.sop.datawarehouse.registration.saga.application.ports.out.consent;

import java.util.UUID;

import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;

public interface ConsentPort {
    ConsentResponse getById(UUID id);
}
