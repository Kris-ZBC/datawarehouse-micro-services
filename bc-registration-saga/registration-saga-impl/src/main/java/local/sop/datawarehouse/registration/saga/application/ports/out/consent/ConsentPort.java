package local.sop.datawarehouse.registration.saga.application.ports.out.consent;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ConsentPort {
	UUID grant(GrantConsentCmd payload);
	ConsentResponse getById(UUID id);
	ConsentStatementResponse getConsentStatementById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
