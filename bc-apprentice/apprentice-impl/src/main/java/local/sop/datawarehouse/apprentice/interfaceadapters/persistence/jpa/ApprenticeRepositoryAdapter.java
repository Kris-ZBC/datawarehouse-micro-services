package local.sop.datawarehouse.apprentice.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.apprentice.domain.model.Apprentice;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.ApprenticeId;
import local.sop.datawarehouse.apprentice.domain.ports.out.ApprenticeRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
@Qualifier("JpaApprenticeRepository")
public class ApprenticeRepositoryAdapter implements ApprenticeRepositoryPort {
    private final ApprenticeSpringDataRepository jpaRepository;
    private static final Logger log = LoggerFactory.getLogger(ApprenticeRepositoryAdapter.class);

    public ApprenticeRepositoryAdapter(ApprenticeSpringDataRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Apprentice save(Apprentice apprentice) {
        ApprenticeEntity entity = ApprenticeJpaMapper.toEntity(apprentice);
        if (entity.getId() == null) {
            entity = entity.withId(UUID.randomUUID());
        }
        ApprenticeEntity saved = jpaRepository.save(entity);
        log.info("saved record with key {}", entity.getId().toString());
        return ApprenticeJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Apprentice> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(ApprenticeJpaMapper::toDomain);
    }

    @Override
    public List<Apprentice> findByEducationLineId(UUID educationLineId) {
        return jpaRepository.findByEducationLineRef(educationLineId)
                            .stream()
                            .map(ApprenticeJpaMapper::toDomain)
                            .toList();
    }

    @Override
    public List<Apprentice> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(ApprenticeJpaMapper::toDomain)
                .toList();
    }
    @Override
    public Boolean compensate(ApprenticeId id, SagaOutcome sagaState) {
        if(sagaState != SagaOutcome.COMPENSATE)
            throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
        var found = findById(id.value());
        if(found == null) {
            return false;
        }
        return (jpaRepository.delete(id.value()) == 1? true: false);
    }
}