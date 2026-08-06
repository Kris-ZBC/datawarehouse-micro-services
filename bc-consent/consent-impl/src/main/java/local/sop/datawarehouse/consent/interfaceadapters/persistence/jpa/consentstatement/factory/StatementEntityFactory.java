package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory;

import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;

public interface StatementEntityFactory {
    ConsentStatementEntity createStatementEntity(ConsentStatementRef statementRef);
}
