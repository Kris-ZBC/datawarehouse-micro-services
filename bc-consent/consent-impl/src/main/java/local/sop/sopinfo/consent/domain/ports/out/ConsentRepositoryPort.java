package local.sop.sopinfo.consent.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.consent.domain.model.Consent;
import local.sop.sopinfo.sharedkernel.enums.ConsentPurpose;
import local.sop.sopinfo.sharedkernel.enums.ConsentStatus;
import local.sop.sopinfo.sharedkernel.enums.ConsentType;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

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
