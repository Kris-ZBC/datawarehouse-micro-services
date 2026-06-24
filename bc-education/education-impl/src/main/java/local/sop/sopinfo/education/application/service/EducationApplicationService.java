package local.sop.sopinfo.education.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.education.application.api.EducationDirectory;
import local.sop.sopinfo.education.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.application.api.dto.EducationResponse;
import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationCategory;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationId;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationName;
import local.sop.sopinfo.education.domain.ports.out.EducationRepositoryPort;
import local.sop.sopinfo.education.domain.service.EducationDomain;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class EducationApplicationService implements EducationDirectory {

    private final EducationRepositoryPort repository;
    private static final Logger log = LoggerFactory.getLogger(EducationApplicationService.class);

    public EducationApplicationService(
            EducationDomain domain,
            @Qualifier("jpaEducationRepository") EducationRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public EducationResponse createEducation(CreateEducationCmd cmd) {
        log.info("Creating new education with name: {} and category: {}", cmd.name(), cmd.category());

        if (repository.existsByName(new EducationName(cmd.name()))) {
            log.warn("Create failed: Education name '{}' already exists", cmd.name());
            throw new ValidationException("education.name.exists", Map.of("name", cmd.name()));
        }

        Education education = Education.builder()
            .name(new EducationName(cmd.name()))
            .category(new EducationCategory(cmd.category()))
            .active(false)
            .build();

        Education saved = repository.create(education);
        log.info("Successfully created education with ID: {}", saved.getId().value());
        return toResponse(saved);
    }

    @Override
    public EducationResponse updateEducationName(UUID id, String name) {
        log.info("Updating name of education ID: {} to '{}'", id, name);
    
        Education existing = repository.findById(EducationId.parse(id.toString()))
            .orElseThrow(() -> {
                log.warn("Education not found for name update: {}", id);
                return new NotFoundException("education.not.found", Map.of("id", id));
            });

        if (!existing.getName().value().equals(name) && repository.existsByName(new EducationName(name))) {
            log.warn("Name update failed: New name '{}' is already taken", name);
            throw new ValidationException("education.name.exists", Map.of("name", name));
        }

        Education updated = Education.builder()
            .id(existing.getId())
            .name(new EducationName(name))
            .category(existing.getCategory())
            .active(existing.isActive())
            .build();

        Education saved = repository.updateName(updated);
        log.info("Successfully updated name of education ID: {}", saved.getId().value());
        return toResponse(saved);
    }

    @Override
    public EducationResponse updateEducationCategory(UUID id, String category) {
        log.info("Updating category of education ID: {} to '{}'", id, category);
    
        Education existing = repository.findById(EducationId.parse(id.toString()))
            .orElseThrow(() -> {
                log.warn("Education not found for category update: {}", id);
                return new NotFoundException("education.not.found", Map.of("id", id));
            });
            
        if (!existing.getCategory().value().equals(category) && repository.existsByCategory(new EducationCategory(category))) {
            log.warn("Category update failed: New category '{}' is already taken", category);
            throw new ValidationException("education.category.exists", Map.of("category", category));
        }

        Education updated = Education.builder()
            .id(existing.getId())
            .name(existing.getName())
            .category(new EducationCategory(category))
            .active(existing.isActive())
            .build();

        Education saved = repository.updateCategory(updated);
        log.info("Successfully updated category of education ID: {}", saved.getId().value());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EducationResponse> findById(UUID id) {
        log.info("Finding education with id: {}", id);
        Optional<Education> education = repository.findById(new EducationId(id));

        if (education.isEmpty()) {
            log.warn("Education not found: {}", id);
            return Optional.empty();
        }

        EducationResponse response = new EducationResponse(
            education.get().getId().value(),
            education.get().getName().value(),
            education.get().getCategory().value(),
            education.get().isActive()
        );

        log.info("Found education: {}", id);
        return Optional.of(response);
    }

    @Override
    public List<EducationResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EducationResponse activateEducation(UUID id) {
        EducationId educationId = EducationId.parse(id.toString());
        Education existing = repository.findById(educationId)
            .orElseThrow(() -> {
                log.warn("Education not found {}", id);
                return new NotFoundException("education.not.found", Map.of("id", id));
        });
        if (existing.isActive()) {
            log.warn("Education already active: {}", id);
            throw new ValidationException("education.already.active", Map.of("id", id));
        }
        Education activated = Education.builder()
            .id(existing.getId())
            .name(existing.getName())
            .category(existing.getCategory())
            .active(true)
            .build();

        Education saved = repository.activate(activated);
        return toResponse(saved);
    }

    @Override
    public EducationResponse deactivateEducation(UUID id) {
        EducationId educationId = EducationId.parse(id.toString());
        Education existing = repository.findById(educationId)
            .orElseThrow(() -> {
                log.warn("Education not found for deactivation: {}", id);
                return new NotFoundException("education.not.found", Map.of("id", id));
        });
        if (!existing.isActive()) {
            log.warn("Education already inactive: {}", id);
            throw new ValidationException("education.already.inactive", Map.of("id", id));
        }
        Education deactivated = Education.builder()
            .id(existing.getId())
            .name(existing.getName())
            .category(existing.getCategory())
            .active(false)
            .build();

        Education saved = repository.deactivate(deactivated);
        return toResponse(saved);
    }

    private EducationResponse toResponse(Education education) {
        return new EducationResponse(
                education.getId().value(),
                education.getName().value(),
                education.getCategory().value(),
                education.isActive()
        );
    }

    @Override
    public ResponseCompensated compensateCreateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensating education line with id: {}", clazz.getSimpleName());

        EducationId educationId = new EducationId(id);
        Optional<Education> response = repository.findById(educationId);

        if (response == null || response.isEmpty()) {
            log.warn("Education not found for compensation: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        
        boolean result = repository.compensateCreate(educationId, sagaState);
        if (result) {
			log.info("Compensation successful for education: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
    }

    @Override
    public ResponseCompensated compensateUpdateEducationName(UUID id, Class<?> clazz, SagaOutcome sagaState,
            String previousName) {
        log.info("Compensating update education name with id: {}", clazz.getSimpleName());

        EducationId educationId = new EducationId(id);
        Optional<Education> response = repository.findById(educationId);

        if (response == null || response.isEmpty()) {
            log.warn("Education not found for compensation: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }

        if (response.get().getName().value().equals(previousName)) {
            log.info("Education name already in previous state, compensation is idempotent: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
        }

        boolean result = repository.compensateUpdateName(educationId, sagaState, previousName);
        if (result) {
            log.info("Compensation successful for education: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
    }

    @Override
    public ResponseCompensated compensateUpdateEducationCategory(UUID id, Class<?> clazz, SagaOutcome sagaState,
            String previousCategory) {
        EducationId educationId = new EducationId(id);
        Optional<Education> response = repository.findById(educationId);

        if (response == null || response.isEmpty()) {
            log.warn("Education not found for compensation: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }

        if (response.get().getCategory().value().equals(previousCategory)) {
            log.info("Education category already in previous state, compensation is idempotent: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
        }

        boolean result = repository.compensateUpdateCategory(educationId, sagaState, previousCategory);
        if (result) {
            log.info("Compensation successful for education: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
    }

    @Override
    public ResponseCompensated compensateActivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        EducationId educationId = new EducationId(id);
        Optional<Education> response = repository.findById(educationId);

        if (response == null || response.isEmpty()) {
            log.warn("Education not found for compensation: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        if (!response.get().isActive()) {
            log.info("Education already inactive, compensation is idempotent: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
        }
        boolean result = repository.compensateActivate(educationId, sagaState);
        if (result) {
            log.info("Compensation successful for education: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
    }

    @Override
    public ResponseCompensated compensateDeactivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        EducationId educationId = new EducationId(id);
        Optional<Education> response = repository.findById(educationId);

        if (response == null || response.isEmpty()) {
            log.warn("Education not found for compensation: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        if (response.get().isActive()) {
            log.info("Education already active, compensation is idempotent: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
        }
        boolean result = repository.compensateDeactivate(educationId, sagaState);
        if (result) {
            log.info("Compensation successful for education: {}", clazz.getSimpleName());
            return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
    }
}