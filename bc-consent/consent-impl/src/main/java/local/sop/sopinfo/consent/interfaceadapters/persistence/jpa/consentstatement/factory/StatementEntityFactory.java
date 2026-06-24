package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.factory;

import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;

public interface StatementEntityFactory {
    ConsentStatementEntity createStatementEntity(ConsentStatementRef statementRef);
}
