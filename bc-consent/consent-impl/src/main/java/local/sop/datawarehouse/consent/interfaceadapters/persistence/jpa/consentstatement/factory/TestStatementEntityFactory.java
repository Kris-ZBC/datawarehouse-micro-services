package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;

// Infrastructure - Test implementation
@Component
@Profile("test")
public class TestStatementEntityFactory implements StatementEntityFactory {
    @Override
    public ConsentStatementEntity createStatementEntity(ConsentStatementRef statementRef) {
        return statementRef != null ?
            ConsentStatementEntity.builder()
                .id(statementRef.value())
                .statementText("test")
                .active(true)
                .build() : null;
    }

}
