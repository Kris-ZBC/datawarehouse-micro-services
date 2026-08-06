package local.sop.datawarehouse.consent.saga.application.ports.out.consent;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentStatementResponse;


public interface ConsentPort {

    UUID create(Boolean active, String text);
    ConsentStatementResponse getStatement(UUID id);
    ConsentResponse getConsent(UUID id);
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ResponseCompensated compensateConsentUpdate(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ConsentResponse grant(UUID personRef, UUID consentStatementRef, ConsentPurpose purpose, ConsentType type, ConsentStatus status);
    ConsentResponse withdraw(UUID consentId);
}
