package local.sop.datawarehouse.consent.saga.application.infrastructure.consent;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadConsentCreate;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadGrantConsent;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadRevokeConsent;
import local.sop.datawarehouse.consent.saga.application.ports.out.consent.ConsentPort;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;

@Component
public class ConsentHttpAdapter implements ConsentPort {
    private final RestClient consent;

    public ConsentHttpAdapter(@Qualifier("consent") RestClient consent) {
        this.consent = consent;
    }
    @Override
    public UUID create(Boolean active, String text, ConsentPurpose purpose, ConsentType type) {
           return consent.post()
                .uri("/internal/consents/statements")
                .body(new PayloadConsentCreate(active, text, purpose, type))
                .retrieve()
                .body(ConsentStatementResponse.class).consentStatementId();
    }

    @Override
    public ConsentStatementResponse getStatement(UUID id) {
        return consent.get()
            .uri("/internal/consents/statements/statement?id={id}", id)
            .retrieve()
            .body(ConsentStatementResponse.class);
    }

    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return consent.post()
            .uri("/internal/consents/statements/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }
    @Override
    public ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return consent.post()
            .uri("/internal/consents/consent/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }
    @Override
    public ConsentResponse grant(UUID personRef, UUID consentStatementRef, ConsentStatus status) {
        return consent.post()
            .uri("/internal/consents/consent/grant")
            .body(new PayloadGrantConsent(personRef, consentStatementRef, status))
            .retrieve()
            .body(ConsentResponse.class);
    }

    @Override
    public ConsentResponse getConsent(UUID id) {
        return consent.get()
            .uri("/internal/consents/consent/{id}", id)
            .retrieve()
            .body(ConsentResponse.class);
    }

    @Override
    public ConsentResponse withdraw(UUID consentId) {
        return consent.post()
            .uri("/internal/consents/consent/withdraw")
            .body(new PayloadRevokeConsent(consentId))
            .retrieve()
            .body(ConsentResponse.class);
    }

    @Override
    public ResponseCompensated compensateConsentUpdate(
            UUID id,
            Class<?> clazz,
            SagaOutcome sagaState) {

        return consent.post()
            .uri("/internal/consents/consent/{id}/compensate/update", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }




}
