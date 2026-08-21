package local.sop.datawarehouse.consent.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;

public interface ConsentStatementRepositoryPort {

    ConsentStatement save(ConsentStatement consentStatement);
 
    Optional<ConsentStatement> findById(DomainId consentStatementId);
 
    List<ConsentStatement> findAll();
 
    // CHANGED: purpose/type added — UpdateConsentStatementCmd already had
    // (unused) fields for these; wiring them through properly rather than
    // leaving them dead. Both nullable: null means "leave unchanged",
    // matching how active/statementText already behave in this method.
	ConsentStatement updateStatement(DomainId consentStatementId, Boolean active, String newStatementText,
	        ConsentPurpose purpose, ConsentType type);
 
    ConsentStatement updateActiveStatus(DomainId consentStatementId, Boolean active);
 
    Boolean compensate(DomainId consentStatementId, SagaOutcome sagaState);
}