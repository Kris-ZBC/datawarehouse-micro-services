package local.sop.datawarehouse.consent.saga.application.api;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.datawarehouse.consent.saga.application.api.dto.GrantConsentCmd;
import local.sop.datawarehouse.consent.saga.application.api.dto.RevokeConsentCmd;

public interface ConsentSagaDirectory {

    ConsentStatementResponse createConsentStatement(CreateConsentStatementCmd cmd);
    ConsentResponse grant(GrantConsentCmd cmd);
    ConsentResponse withdraw(RevokeConsentCmd cmd);
    ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
