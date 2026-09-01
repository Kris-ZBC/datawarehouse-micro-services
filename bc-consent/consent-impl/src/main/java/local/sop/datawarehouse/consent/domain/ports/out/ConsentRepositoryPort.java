package local.sop.datawarehouse.consent.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.Consent;

public interface ConsentRepositoryPort {

    Consent save(Consent consent);

    Optional<Consent> findById(DomainId id);

    Optional<Consent> findByPersonAndStatementReference(DomainId personReference, DomainId statementReference);

    Optional<Consent> findByPersonAndPurpose(DomainId personReference, ConsentPurpose purpose);

    List<Consent> findByPersonReference(DomainId personReference);

    List<Consent> findByStatusAndPurposeAndType(ConsentStatus status, ConsentPurpose purpose, ConsentType type);

    List<Consent> findAll();

	Consent update(Consent consent);

    Boolean compensate(DomainId consentId, SagaOutcome sagaState);
}
