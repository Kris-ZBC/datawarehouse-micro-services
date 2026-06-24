package local.sop.sopinfo.anonymize.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;
import local.sop.sopinfo.anonymize.domain.ports.out.AnonymizeRepositoryPort;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;


@Repository
public class AnonymizeJpaRepositoryAdapter implements AnonymizeRepositoryPort {

    private final AnonymizeSpringDataRepository repository;
    private final AnonymizeJpaMapper mapper;

    public AnonymizeJpaRepositoryAdapter(AnonymizeSpringDataRepository repository, AnonymizeJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Anonymize save(Anonymize anonymizeLog) {
        AnonymizeEntity entity = mapper.toEntity(anonymizeLog);
        AnonymizeEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Anonymize> findById(AnonymizeId anonymizationId) {
        return repository.findById(anonymizationId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Anonymize> findBySearchParams(AnonymizeId anonymizationId, PersonRef personRef) {
        return repository.findByIdOrPersonRef(anonymizationId.value(), personRef.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Anonymize> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Boolean compensate(AnonymizeId anonymizationId, SagaOutcome sagaState) {
        if (sagaState != SagaOutcome.COMPENSATE) {
            throw new ConflictException("compensate.wrong_state", java.util.Map.of("compensate", sagaState.name()));
        }
        Optional<Anonymize> response = findById(anonymizationId);
        if (response.isEmpty()) {
            return false;
        }
        return repository.delete(anonymizationId.value()) == 1;
    }

}
