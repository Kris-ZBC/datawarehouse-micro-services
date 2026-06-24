package local.sop.sopinfo.educationline.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;
import local.sop.sopinfo.educationline.domain.ports.out.EducationLineRepositoryPort;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

// Log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
@Qualifier("JpaEducationLineRepository")
public class EducationLineRepositoryAdapter implements EducationLineRepositoryPort {
	private final EducationLineSpringDataRepository jpaRepository;
	private final EducationLineJpaMapper mapper;
	private static final Logger log = LoggerFactory.getLogger(EducationLineRepositoryAdapter.class);

	public EducationLineRepositoryAdapter(EducationLineSpringDataRepository springDataRepository) {
		this.jpaRepository = springDataRepository;
		this.mapper = new EducationLineJpaMapper();
	}

	@Override
	public EducationLine save(EducationLine educationLine) {
		try {
			EducationLineEntity entity = mapper.toEntity(educationLine);
			if (entity.getId() == null) {
				entity.setId(UUID.randomUUID());
			}
			log.info("Creating new entity wtith id: {}", entity.getId().toString());
			EducationLineEntity savedEntity = jpaRepository.save(entity);
			log.info("New entity created: {}", savedEntity);
			return mapper.toDomain(savedEntity);
		} catch (Exception e) {
			log.error("Error saving entity", e);
			throw new ValidationException("educationline.failed.save", Map.of());
		}
	}

	public List<EducationLine> findBySearchParams(UUID id, String name) {
		try {
			List<EducationLineEntity> entities = jpaRepository.findBySearchParams(id, name);
			return entities.stream()
					.map(mapper::toDomain)
					.toList();
		} catch (Exception e) {
			log.error("Error finding entities with search params", e);
			throw new ValidationException("educationline.failed.findbysearchparams", Map.of());
		}
	}

	@Override
	public List<EducationLine> findAll() {
		try {
			return jpaRepository.findAll().stream()
					.map(mapper::toDomain)
					.toList();
		} catch (Exception e) {
			log.error("Error finding all entities", e);
			throw new ValidationException("educationline.failed.findall", Map.of());
		}
	}

	@Override
	public List<EducationLine> findByEducationRef(EducationRef educationRef) {
		try {
			return jpaRepository.findByEducationRef(educationRef.value()).stream()
					.map(mapper::toDomain)
					.toList();
		} catch (Exception e) {
			log.error("Error finding entity with education ref: {}", educationRef.value(), e);
			throw new ValidationException("educationline.failed.findbyeducationref", Map.of("educationRef", educationRef.value()));
		}
	}

	// Using optional to handle the case where the education line might not be found
	@Override
	public Optional<EducationLine> findById(EducationLineId id) {
		try {
			return jpaRepository.findById(id.value())
					.map(mapper::toDomain);
		} catch (Exception e) {
			log.error("Error finding entity with id: {}", id.value(), e);
			throw new ValidationException("educationline.failed.findbyid", Map.of("id", id.value()));
		}
	}

	@Override
	public Optional<EducationLine> updateEducationLineName(EducationLineId id, EducationLineName name) {
		try {
			if (!jpaRepository.existsById(id.value())) {
				log.warn("Entity not found for name update, with id: {}", id.value());
				return Optional.empty();
			}
			log.info("Updating entity name for id: {}", id.value());
			jpaRepository.update(id.value(), name.value(), null, null, null);
			Optional<EducationLine> updatedEntity = jpaRepository.findById(id.value()).map(mapper::toDomain);
			log.info("Updated entity name: {}", updatedEntity);
			return updatedEntity;
		} catch (Exception e) {
			log.error("Error updating entity name for id: {}", id.value(), e);
			throw new ValidationException("educationline.failed.updatename", Map.of("id", id.value()));
		}
	}

	@Override
	public Optional<EducationLine> updateEducationLineDuration(EducationLineId id, EducationLineDuration duration) {
		try {
			if (!jpaRepository.existsById(id.value())) {
				log.warn("Entity not found for duration update, with id: {}", id.value());
				return Optional.empty();
			}
			jpaRepository.update(id.value(), null, duration.years(), duration.months(), duration.days());
			log.info("Updated entity duration for id: {}", id.value());
			return jpaRepository.findById(id.value()).map(mapper::toDomain);
		} catch (Exception e) {
			log.error("Error updating entity duration for id: {}", id.value(), e);
			throw new ValidationException("educationline.failed.updateduration", Map.of("id", id.value()));
		}
	}

	@Override
	public Optional<EducationLine> deactivate(EducationLineId id) {
		try {
			if (!jpaRepository.existsById(id.value())) {
				log.warn("Entity not found for deactivation, with id: {}", id.value());
				throw new NotFoundException("educationline.not.found", Map.of("id", id.value()));
			}
			jpaRepository.deactivate(id.value());
			log.info("Entity deactivated with id: {}", id.value());
			return jpaRepository.findById(id.value()).map(mapper::toDomain);
		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error deactivating entity with id: {}", id.value(), e);
			throw new ValidationException("educationline.failed.deactivation", Map.of("id", id.value()));
		}
	}

	@Override
	public Optional<EducationLine> activate(EducationLineId id) {
		try {
			if (!jpaRepository.existsById(id.value())) {
				log.warn("Entity not found for activation, with id: {}", id.value());
				throw new NotFoundException("educationline.not.found", Map.of("id", id.value()));
			}
			jpaRepository.activate(id.value());
			log.info("Entity activated with id: {}", id.value());
			return jpaRepository.findById(id.value()).map(mapper::toDomain);
		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error activating entity with id: {}", id.value(), e);
			throw new ValidationException("educationline.failed.activation", Map.of("id", id.value()));
		}
	}
	
	@Override
	public Boolean compensate(EducationLineId id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}
		Optional<EducationLine> response = findById(id);
		if (response == null || response.isEmpty()) {
			log.warn("Education line not found for compensation: {}", id.value());
			return false;
		}
		return (jpaRepository.delete(id.value()) == 1? true: false);
	}

	@Override
	public Boolean compensateActivate(EducationLineId id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationLine> response = findById(id);
		if (response == null || response.isEmpty()) {
			log.warn("Education line not found for compensation activate: {}", id);
			return false;
		}

		return jpaRepository.compensateActivate(id.value());
	}

	@Override
	public Boolean compensateDeactivate(EducationLineId id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationLine> response = findById(id);
		if (response == null || response.isEmpty()) {
			log.warn("Education line not found for compensation deactivate: {}", id);
			return false;
		}

		return jpaRepository.compensateDeactivate(id.value());
	}

	@Override
	public Boolean compensateName(EducationLineId id, SagaOutcome sagaState, EducationLineName nameReq) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationLine> response = findById(id);
		if (response == null || response.isEmpty()) {
			log.warn("Education line not found for compensation deactivate: {}", id);
			return false;
		}

		return jpaRepository.compensateName(id.value(), nameReq.value());
	}

	@Override
	public Boolean compensateDuration(EducationLineId id, SagaOutcome sagaState, EducationLineDuration req) {
		if (sagaState != SagaOutcome.COMPENSATE) {
			log.warn("Invalid saga state for compensation: {}", sagaState);
			throw new ConflictException("compensate.wrong.state", Map.of("compensate", sagaState.name()));
		}

		Optional<EducationLine> response = findById(id);
		if (response == null || response.isEmpty()) {
			log.warn("Education line not found for compensation deactivate: {}", id);
			return false;
		}

		return jpaRepository.compensateDuration(id.value(), req.years(), req.months(), req.days());
	}
}
