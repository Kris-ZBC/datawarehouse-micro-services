package local.sop.sopinfo.registration.saga.application.ports.out.consent;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ConsentPort {
	UUID grant(GrantConsentCmd payload);
	ConsentResponse getById(UUID id);
	ConsentStatementResponse getConsentStatementById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
