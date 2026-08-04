package local.sop.sopinfo.educationinstructor.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.educationinstructor.application.api.EducationInstructorDirectory;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreateEducationInstructorCmd;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreatedEducationInstructorResult;
import local.sop.sopinfo.educationinstructor.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.educationinstructor.application.api.dto.ToggleActivateEducationInstructorCmd;
import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.sopinfo.educationinstructor.domain.ports.out.EducationInstructorRepositoryPort;
import local.sop.sopinfo.educationinstructor.domain.service.EducationInstructorDomain;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.compositekey.validate.ValidateCompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EducationInstructorApplicationService implements EducationInstructorDirectory {

	private final EducationInstructorRepositoryPort repository;

	private final EducationInstructorDomain domain;
	
	private static final Logger log = LoggerFactory.getLogger(EducationInstructorApplicationService.class);

	public EducationInstructorApplicationService(
            EducationInstructorRepositoryPort repository,
            EducationInstructorDomain domain) {
        this.repository = repository;
        this.domain = domain;
    }

	@Override
    @Transactional
    @ValidateCompositeKey(ports = {"educationPort", "instructorPort"})
    public CreatedEducationInstructorResult create(CreateEducationInstructorCmd command) {

		log.info("Creating EducationInstructor with id: {}, active: {}", command.id(), command.active());

        EducationInstructor aggregate = domain.createEducationInstructor(command.id(), command.active());

        EducationInstructor saved = repository.save(aggregate);

		log.info("Successfully created EducationInstructor with id: {}, active: {}", saved.getId(), saved.isActive());

        return new CreatedEducationInstructorResult(saved.getId());
    }

	@Override
    @Transactional
    @ValidateCompositeKey(ports = {"educationPort", "instructorPort"})
    public EducationInstructorResponse toggleActive(ToggleActivateEducationInstructorCmd command) {

		log.info("Toggling active status for EducationInstructor with id: {}, active: {}", command.id(), command.active());

        Optional<EducationInstructor> existingOptional = repository.findById(command.id());

        if (existingOptional.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for toggling active status", command.id());

            throw new NotFoundException("key.not.found",Map.of("field", "id", "value", command.id()));
        }

        EducationInstructor existing = existingOptional.get();
        EducationInstructor updated = domain.toggleActivateEducationInstructor(command.id(),command.active(),existing.getCreatedAt().value());

        repository.update(updated);
		log.info("Successfully toggled active status for EducationInstructor with id: {}, new active status: {}", updated.getId(), updated.isActive());
        return new EducationInstructorResponse(updated.getId(), updated.isActive(), updated.getCreatedAt().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EducationInstructorResponse> findById(CompositeKey id) {
        return Optional.ofNullable(repository.findById(id)
                        .map(s -> new EducationInstructorResponse(s.getId(),s.isActive(),s.getCreatedAt().value()))
                        .orElseThrow(() -> new NotFoundException("key.not.found",Map.of("field", "id", "value", id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationInstructorResponse> getAll() {
        return repository.findAll().stream()
                .map(s -> new EducationInstructorResponse(s.getId(),s.isActive(),s.getCreatedAt().value()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationInstructorResponse> getByEducationRef(UUID educationRef) {
        return repository.findByEducationRef(educationRef).stream()
			.map(s -> new EducationInstructorResponse(s.getId(),s.isActive(),s.getCreatedAt().value()))
			.toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationInstructorResponse> getByInstructorRef(UUID instructorRef) {
        return repository.findByInstructorRef(instructorRef).stream()
                .map(s -> new EducationInstructorResponse(s.getId(),s.isActive(),s.getCreatedAt().value()))
                .toList();
    }

	@Override
	public ResponseCompensated compensateCreateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState) {
		
		log.info("Compensating createEducationInstructor for id: {}, class: {}, sagaState: {}", id,
				clazz.getSimpleName(), sagaState);
		
		Optional<EducationInstructor> existing = repository.findById(id);

		if (existing.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for compensation", id);
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateCreateEducationInstructor(id, sagaState);
		if (result) {
			log.info("Compensation for createEducationInstructor successful for id: {}", id);
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
	}

	@Override
	public ResponseCompensated compensateActivateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState) {
		
		log.info("Compensating activateEducationInstructor for id: {}, class: {}, sagaState: {}", id,
				clazz.getSimpleName(), sagaState);
		
		Optional<EducationInstructor> existing = repository.findById(id);

		if (existing.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for compensation", id);
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		if (!existing.get().isActive()) {
			log.info("EducationInstructor with id {} is already deactivated, compensation is idempotent", id);
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateActivateEducationInstructor(id, sagaState);
		if (result) {
			log.info("Compensation for activateEducationInstructor successful for id: {}", id);
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
	}

	@Override
	public ResponseCompensated compensateDeactivateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState) {
		log.info("Compensating deactivateEducationInstructor for id: {}, class: {}, sagaState: {}", id,
				clazz.getSimpleName(), sagaState);
		
		Optional<EducationInstructor> existing = repository.findById(id);

		if (existing.isEmpty()) {
			log.warn("EducationInstructor with id {} not found for compensation", id);
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		if (existing.get().isActive()) {
			log.warn("EducationInstructor with id {} is already active, no compensation needed", id);
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateDeactivateEducationInstructor(id, sagaState);
		if (result) {
			log.info("Compensation for deactivateEducationInstructor successful for id: {}", id);
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
	}
}
