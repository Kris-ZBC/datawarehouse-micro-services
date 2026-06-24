package local.sop.sopinfo.educationinstructor.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.sopinfo.educationinstructor.domain.ports.out.EducationInstructorRepositoryPort;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

import org.springframework.stereotype.Repository;

// Log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
//@Qualifier("JpaEducationInstructorRepository")
public class EducationInstructorRepositoryAdapter implements EducationInstructorRepositoryPort {

    private final EducationInstructorSpringDataRepository repository;
    private final EducationInstructorJpaMapper mapper;
	private static final Logger log = LoggerFactory.getLogger(EducationInstructorRepositoryAdapter.class);

    public EducationInstructorRepositoryAdapter(EducationInstructorSpringDataRepository repository,EducationInstructorJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<EducationInstructor> findById(CompositeKey id) {
        EducationInstructorId educationInstructorId = new EducationInstructorId(id.key1(), id.key2());
        return Optional.ofNullable(repository.findById(educationInstructorId)
			.map(mapper::toDomain)
			.orElse(null));
    }

    @Override
    public EducationInstructor save(EducationInstructor e) {
        EducationInstructorEntity entity = mapper.toEntity(e);
        entity = repository.save(entity);
        entity = repository.findById(entity.getId()).orElseThrow(() -> new NotFoundException("key.not.found",Map.of("field", "id","id", e.getId())));
        return mapper.toDomain(entity);
    }

    @Override
    public void update(EducationInstructor e) {

        EducationInstructor existing = findById(e.getId())
			.orElseThrow(() -> new NotFoundException("key.not.found",Map.of("field", "id","id", e.getId().toString())));
        EducationInstructorEntity entity = mapper.toEntity(existing);
        mapper.updateIntoEntity(e, entity);
        repository.updateActiveById(entity.getId(),e.isActive());
    }

    @Override
    public List<EducationInstructor> findAll() {
        return repository.findAll().stream()
			.map(mapper::toDomain)
			.toList();
    }

    @Override
    public List<EducationInstructor> findByEducationRef(UUID educationRef) {
        return repository.findByEducationRef(educationRef).stream()
			.map(mapper::toDomain)
			.toList();
    }

    @Override
    public List<EducationInstructor> findByInstructorRef(UUID instructorRef) {
        return repository.findByInstructorRef(instructorRef).stream()
			.map(mapper::toDomain)
			.toList();
    }

	// Compensation methods

	@Override
	public Boolean compensateCreateEducationInstructor(CompositeKey id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationInstructor> existing = findById(id);
		if (existing.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for compensation", id);
			return false;
		}
		repository.delete(id.key1(), id.key2());
		return true;
	}

	@Override
	public Boolean compensateActivateEducationInstructor(CompositeKey id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationInstructor> existing = findById(id);
		if (existing.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for compensation", id);
			return false;
		}
		repository.compensateActivateByEducationRefAndInstructorRef(id.key1(), id.key2());
		return true;
	}

	@Override
	public Boolean compensateDeactivateEducationInstructor(CompositeKey id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationInstructor> existing = findById(id);
		if (existing.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for compensation", id);
			return false;
		}
		repository.compensateDeactivateByEducationRefAndInstructorRef(id.key1(), id.key2());
		return true;
	}
}
