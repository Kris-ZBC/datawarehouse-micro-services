package local.sop.datawarehouse.education.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.education.domain.model.Education;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationCategory;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationId;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationName;
import local.sop.datawarehouse.education.domain.ports.out.EducationRepositoryPort;

// Log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository

@Qualifier("jpaEducationRepository")
public class EducationRepositoryAdapter implements EducationRepositoryPort {

    private final EducationSpringDataRepository jpaRepository;
    private final EducationJpaMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(EducationRepositoryAdapter.class);

    public EducationRepositoryAdapter(EducationSpringDataRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = new EducationJpaMapper();
    }

    @Override
    public Education create(Education education) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(education)));
    }

    @Override
    public boolean existsByName(EducationName name) {
        return jpaRepository.existsByName(name.value()); // .value() extracts the String
    }

    @Override
    public boolean existsByCategory(EducationCategory category) {
        return jpaRepository.existsByCategory(category.value()); // .value() extracts the String
    }

    @Override
    public Education updateName(Education education) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(education)));
    }

    @Override
    public Education updateCategory(Education education) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(education)));
    }

    @Override
    public Optional<Education> findById(EducationId id) {
        return jpaRepository.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public List<Education> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Education activate(Education education) {
        jpaRepository.activate(education.getId().value());
        return jpaRepository.findById(education.getId().value()).map(mapper::toDomain).orElse(null);
    }

    @Override
    public Education deactivate(Education education) {
        jpaRepository.deactivate(education.getId().value());
        return jpaRepository.findById(education.getId().value()).map(mapper::toDomain).orElse(null);
    }

    @Override
    public Boolean compensateCreate(EducationId id, SagaOutcome sagaState) {
        if (sagaState != SagaOutcome.COMPENSATE) {
            log.warn("Invalid saga state for compensation: {}", sagaState);
            throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
        }
        Optional<Education> response = findById(id);
        if (response.isEmpty()) {
            log.info("Education not found for compensation, assuming already compensated: {}", id);
        }
        jpaRepository.deleteById(id.value());
        return true;
    }

    @Override
    public Boolean compensateActivate(EducationId id, SagaOutcome sagaState) {
        if (sagaState != SagaOutcome.COMPENSATE) {
            log.warn("Invalid saga state for compensation: {}", sagaState);
            throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
        }
        Optional<Education> response = findById(id);
        if (response.isEmpty()) {
            log.info("Education not found for compensation, assuming already compensated: {}", id);
        }
        jpaRepository.updateActive(id.value(), false);
        return true;
    }

    @Override
    public Boolean compensateDeactivate(EducationId id, SagaOutcome sagaState) {
        if (sagaState != SagaOutcome.COMPENSATE) {
            log.warn("Invalid saga state for compensation: {}", sagaState);
            throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
        }
        Optional<Education> response = findById(id);
        if (response.isEmpty()) {
            log.info("Education not found for compensation, assuming already compensated: {}", id);
        }
        jpaRepository.updateActive(id.value(), true);
        return true;
    }

    @Override
    public Boolean compensateUpdateName(EducationId id, SagaOutcome sagaState, String oldName) {
        if (sagaState != SagaOutcome.COMPENSATE) {
            log.warn("Invalid saga state for compensation: {}", sagaState);
            throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
        }
        Optional<Education> response = findById(id);
        if (response.isEmpty()) {
            log.info("Education not found for compensation, assuming already compensated: {}", id);
        }
        jpaRepository.updateName(id.value(), oldName);
        return true;
    }

    @Override
    public Boolean compensateUpdateCategory(EducationId id, SagaOutcome sagaState, String oldCategory) {
        if (sagaState != SagaOutcome.COMPENSATE) {
            log.warn("Invalid saga state for compensation: {}", sagaState);
            throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
        }
        Optional<Education> response = findById(id);
        if (response.isEmpty()) {
            log.info("Education not found for compensation, assuming already compensated: {}", id);
        }
        jpaRepository.updateCategory(id.value(), oldCategory);
        return true;
    }
}
