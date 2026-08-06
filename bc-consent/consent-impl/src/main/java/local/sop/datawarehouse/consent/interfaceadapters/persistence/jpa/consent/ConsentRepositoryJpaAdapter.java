package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consent;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentRepositoryPort;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementSpringDataRepository;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


@Repository
public class ConsentRepositoryJpaAdapter implements ConsentRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(ConsentRepositoryJpaAdapter.class);
    private final ConsentSpringDataRepository repository;
    private final ConsentStatementSpringDataRepository statementRepository;
    private final ConsentJpaMapper mapper;

    public ConsentRepositoryJpaAdapter(
            ConsentSpringDataRepository repository,
            ConsentStatementSpringDataRepository statementRepository,
            ConsentJpaMapper mapper
    ) {
        this.repository = repository;
        this.statementRepository = statementRepository;
        this.mapper = mapper;
    }

    @Override
    public Consent save(Consent consent) {
        ConsentEntity entity = mapper.toEntity(consent);
        ConsentEntity savedEntity = repository.save(entity);
        var statementEntity = statementRepository.findById(savedEntity.getConsentStatement().getId()).orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", savedEntity.getConsentStatement().getId())));
        Set<ConsentEntity> allConsents = new HashSet<>(statementEntity.getConsents());
        allConsents.add(savedEntity);
        statementEntity.withConsents(allConsents);
        statementRepository.save(statementEntity);
        log.info("Saved consent with id: {}", savedEntity.getId());
        log.info("Associated consent statement id: {}", statementEntity.getId());
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Consent> findById(DomainId id) {
        return repository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Consent> findByPersonAndStatementReference(DomainId personReference, DomainId statementReference) {
        return repository.findByPersonAndStatementReference(personReference.value(), statementReference.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Consent> findByPersonAndPurpose(DomainId personReference, ConsentPurpose purpose) {
        return repository.findByPersonAndPurpose(personReference.value(), purpose)
                .map(mapper::toDomain);
    }

    @Override
    public List<Consent> findByPersonReference(DomainId personReference) {
        return repository.findByPersonReference(personReference.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Consent> findByStatusAndPurposeAndType(ConsentStatus status, ConsentPurpose purpose, ConsentType type) {
        return repository.findByStatusAndPurposeAndType(status, purpose, type).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Consent> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Consent update(Consent consent) {
        ConsentEntity entity = repository.findById(consent.getId().value())
                .orElseThrow(() -> new NotFoundException("consent.notfound", Map.of("id", consent.getId().value())));
        entity = entity.withStatus(consent.getStatus())
                       .withConsentPurpose(consent.getPurpose())
                       .withConsentType(consent.getType());
        ConsentEntity updatedEntity = repository.save(entity);

        var statementEntity = statementRepository.findById(updatedEntity.getConsentStatement().getId()).orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", updatedEntity.getConsentStatement().getId())));

        /* verify that at least one statement is active, else disable the statement */
        boolean anyActive = statementEntity.getConsents().stream()
                .filter(c -> !c.getId().equals(updatedEntity.getId())) // Exclude the updated consent
                .anyMatch(c -> c.getStatus() == ConsentStatus.ACTIVE) || updatedEntity.getStatus() == ConsentStatus.ACTIVE; // Include the updated consent's new status

        if (!anyActive) {
            statementEntity.withActive(false);
            statementRepository.save(statementEntity);
        }

        log.info("Updated consent with id: {}", updatedEntity.getId());
        return mapper.toDomain(updatedEntity);
    }

    @Override
    public Boolean compensate(DomainId consentId, SagaOutcome sagaState) {
        if(sagaState != SagaOutcome.COMPENSATE)
            throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
        var found = findById(consentId);
        if(found == null) {
            return false;
        }
        return (repository.delete(consentId.value()) == 1? true: false);
    }


}