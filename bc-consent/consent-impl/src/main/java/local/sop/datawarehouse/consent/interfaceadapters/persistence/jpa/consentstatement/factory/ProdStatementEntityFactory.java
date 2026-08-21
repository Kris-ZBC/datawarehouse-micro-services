package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementSpringDataRepository;

// Infrastructure - Production implementation
@Component
@Profile("!test")
public class ProdStatementEntityFactory implements StatementEntityFactory {
    private final ConsentStatementSpringDataRepository repository;
 
    public ProdStatementEntityFactory(ConsentStatementSpringDataRepository repository) {
        this.repository = repository;
    }
    @Override
    public ConsentStatementEntity createStatementEntity(ConsentStatementRef statementRef) {
        if (statementRef == null) return null;
        return repository.findById(statementRef.value())
                .orElseThrow(() -> new ValidationException(
                        "consentstatement.notfound",
                        Map.of("field", "consentStatementRef", "id", statementRef.value())));
    }
 
}
