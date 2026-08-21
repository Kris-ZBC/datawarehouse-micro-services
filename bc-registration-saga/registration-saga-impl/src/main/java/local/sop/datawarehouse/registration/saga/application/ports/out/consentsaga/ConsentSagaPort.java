package local.sop.datawarehouse.registration.saga.application.ports.out.consentsaga;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ConsentSagaPort {
	ConsentResponse grant(GrantConsentCmd payload);
	ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
