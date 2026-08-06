package local.sop.datawarehouse.consent.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;

public interface ConsentStatementRepositoryPort {

    ConsentStatement save(ConsentStatement consentStatement);

    Optional<ConsentStatement> findById(DomainId consentStatementId);

    List<ConsentStatement> findAll();

	ConsentStatement updateStatement(DomainId consentStatementId, Boolean active, String newStatementText);

    ConsentStatement updateActiveStatus(DomainId consentStatementId, Boolean active);

    Boolean compensate(DomainId consentStatementId, SagaOutcome sagaState);
}