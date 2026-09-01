package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;

// Infrastructure - Test implementation
@Component
@Profile("test")
public class TestStatementEntityFactory implements StatementEntityFactory {
    @Override
    public ConsentStatementEntity createStatementEntity(ConsentStatementRef statementRef) {
        // CHANGED: purpose/type are now required on ConsentStatementEntity
        // (see its Builder), so this stub needs defaults to satisfy that.
        return statementRef != null ?
            ConsentStatementEntity.builder()
                .id(statementRef.value())
                .statementText("test")
                .purpose(ConsentPurpose.REQUIRED_SERVICE)
                .type(ConsentType.REQUIRED)
                .active(true)
                .build() : null;
    }

}
