package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentStatementRepositoryPort;

@Repository
public class ConsentStatementRepositoryJpaAdapter implements ConsentStatementRepositoryPort {
 
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConsentStatementRepositoryJpaAdapter.class);
    private final ConsentStatementSpringDataRepository repository;
    private final ConsentStatementJpaMapper mapper;
 
    public ConsentStatementRepositoryJpaAdapter(
            ConsentStatementSpringDataRepository repository,
            ConsentStatementJpaMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }
 
    @Override
    public ConsentStatement save(ConsentStatement consentStatement) {
        ConsentStatementEntity entity = mapper.toEntity(consentStatement);
        ConsentStatementEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
 
    @Override
    public List<ConsentStatement> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
 
 
    @Override
    public Optional<ConsentStatement> findById(DomainId consentStatementId) {
        return repository.findById(consentStatementId.value())
                .map(mapper::toDomain);
    }
 
    @Override
    public ConsentStatement updateStatement(DomainId consentStatementId, Boolean active, String newStatementText,
            ConsentPurpose purpose, ConsentType type) {
            int rows = repository.updateStatement(consentStatementId.value(), active, newStatementText, purpose, type);
            log.info("Updated consent statement " + rows);
            return findById(consentStatementId).orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", consentStatementId.value())));
    }
 
    @Override
    public ConsentStatement updateActiveStatus(DomainId consentStatementId, Boolean active) {
        repository.updateActiveStatus(consentStatementId.value(), active);
        return findById(consentStatementId).orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", consentStatementId.value())));
    }
 
    @Override
    public Boolean compensate(DomainId id, SagaOutcome sagaState) {
        if(sagaState != SagaOutcome.COMPENSATE)
            throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
        var found = findById(id);
        if(found == null) {
            return false;
        }
        return (repository.delete(id.value()) == 1? true: false);
    }
 
}
 

    
