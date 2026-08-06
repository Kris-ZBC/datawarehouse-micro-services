package local.sop.datawarehouse.consent.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.application.api.dto.*;

public interface ConsentDirectory extends Compensatable {

    /*  CRUD Directory for the controller */


    ConsentStatementResponse createConsentStatement(CreateConsentStatementCmd cmd);
    
    ConsentResponse grantConsent(GrantConsentCmd cmd);

    Optional<ConsentResponse> getConsent(UUID consentId);

    ConsentResponse withdrawConsent(RevokeConsentCmd cmd);

    ConsentStatementResponse updateConsentStatement(UpdateConsentStatementCmd cmd);

    Optional<ConsentStatementResponse> getConsentStatement(FetchConsentStatementQuery query);

    // fetch all consents
    List<ConsentResponse> getAllConsents();
    
    // fetch all consent statements
    List<ConsentStatementResponse> getAllConsentStatements();

    //fetch all consents for a person
    List<ConsentResponse> getAllConsentsForPerson(FetchAllConsentsForPersonQuery query);

    Optional<ConsentResponse> getConsentForPersonAndPurpose(FetchConsentForPersonAndPurposeQuery query);

    ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState);

    ResponseCompensated compensateConsentWithdrawalUpdate(UUID id, Class<?> clazz, SagaOutcome sagaState);

}