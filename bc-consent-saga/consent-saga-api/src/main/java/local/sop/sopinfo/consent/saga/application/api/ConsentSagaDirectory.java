package local.sop.sopinfo.consent.saga.application.api;

import local.sop.sopinfo.consent.saga.application.api.dto.ConsentResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.GrantConsentCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.RevokeConsentCmd;

public interface ConsentSagaDirectory {

    ConsentStatementResponse createConsentStatement(CreateConsentStatementCmd cmd);
    ConsentResponse grant(GrantConsentCmd cmd);
    ConsentResponse withdraw(RevokeConsentCmd cmd);
}
