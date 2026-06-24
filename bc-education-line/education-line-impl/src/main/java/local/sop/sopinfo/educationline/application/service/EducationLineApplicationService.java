package local.sop.sopinfo.educationline.application.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.educationline.application.api.EducationLineDirectory;
import local.sop.sopinfo.educationline.application.api.dto.CreateEducationLineCmd;
import local.sop.sopinfo.educationline.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineDurationCmd;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineNameCmd;
import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.*;
import local.sop.sopinfo.educationline.domain.ports.out.EducationLineRepositoryPort;
import local.sop.sopinfo.educationline.domain.service.EducationLineDomain;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EducationLineApplicationService implements EducationLineDirectory {
	private final EducationLineDomain domain;
	private final EducationLineRepositoryPort repository;
	private static final Logger log = LoggerFactory.getLogger(EducationLineApplicationService.class);

	public EducationLineApplicationService(EducationLineDomain domain, @Qualifier("JpaEducationLineRepository") EducationLineRepositoryPort repository) {
		this.domain = domain;
		this.repository = repository;
	}

	@Override
	@Transactional
	public EducationLineResponse createEducationLine(CreateEducationLineCmd cmd) {
		log.info("Creating an education line: {}", cmd);
		EducationLineId id = EducationLineId.newId();
		EducationLineName name = new EducationLineName(cmd.name());
		EducationLineDuration duration = new EducationLineDuration(cmd.durationYears(), cmd.durationMonths(), cmd.durationDays());
		EducationRef educationRef = new EducationRef(cmd.educationRef());
		EducationLineCreatedAt createdAt = EducationLineCreatedAt.now();
		Boolean active = true;

		EducationLine educationLine = EducationLine.create(id, name, duration, createdAt, educationRef, active);
		EducationLine createdEducationLine = domain.createEducationLine(educationLine);
		EducationLine savedEducationLine = repository.save(createdEducationLine);

		log.info("Education line created: {}", savedEducationLine.getId());

		return new EducationLineResponse(
				savedEducationLine.getId().value(),
				savedEducationLine.getName().value(),
				savedEducationLine.getDuration().years(),
				savedEducationLine.getDuration().months(),
				savedEducationLine.getDuration().days(),
				savedEducationLine.getEducationRef().value(),
				savedEducationLine.getCreatedAt().value(),
				savedEducationLine.isActive()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EducationLineResponse> findAll() {
		log.info("Finding all education lines");
		List<EducationLine> educationLines = repository.findAll();
		List<EducationLineResponse> responses = new ArrayList<>();

		for (EducationLine educationLine : educationLines) {
			responses.add(new EducationLineResponse(
					educationLine.getId().value(),
					educationLine.getName().value(),
					educationLine.getDuration().years(),
					educationLine.getDuration().months(),
					educationLine.getDuration().days(),
					educationLine.getEducationRef().value(),
					educationLine.getCreatedAt().value(),
					educationLine.isActive()
			));
		}
		log.info("Found {} education lines", responses.size());
		return responses;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<EducationLineResponse> findById(UUID id) {
		log.info("Finding education line with id: {}", id);
		Optional<EducationLine> educationLine = repository.findById(new EducationLineId(id));

		if (educationLine.isEmpty()) {
			log.warn("Education line not found: {}", id);
			throw new NotFoundException("educationline.not.found", Map.of("id", id));
		}

		EducationLineResponse response = new EducationLineResponse(
				educationLine.get().getId().value(),
				educationLine.get().getName().value(),
				educationLine.get().getDuration().years(),
				educationLine.get().getDuration().months(),
				educationLine.get().getDuration().days(),
				educationLine.get().getEducationRef().value(),
				educationLine.get().getCreatedAt().value(),
				educationLine.get().isActive()
		);
		log.info("Found education line: {}", response);
		return Optional.of(response);
	}

	@Override 
	@Transactional(readOnly = true)
	public List<EducationLineResponse> findByEducationRef(UUID id){
		log.info("Finding education line with education ref: {}", id);
		List<EducationLine> educationLine = repository.findByEducationRef(new EducationRef(id));

		if (educationLine.isEmpty()) {
			log.warn("Education line not found for education ref: {}", id);
			throw new NotFoundException("educationline.not.found", Map.of("educationRef", id));
		}

		List<EducationLineResponse> responses = new ArrayList<>();
		for (EducationLine el : educationLine) {
			responses.add(new EducationLineResponse(
					el.getId().value(),
					el.getName().value(),
					el.getDuration().years(),
					el.getDuration().months(),
					el.getDuration().days(),
					el.getEducationRef().value(),
					el.getCreatedAt().value(),
					el.isActive()
			));
		}
		log.info("Found {} education lines for education ref: {}", responses.size(), id);
		return responses;
	}

	@Override
	@Transactional
	public EducationLineResponse updateEducationLineName(UUID id, UpdateEducationLineNameCmd request) {
		log.info("Updating education line name for id: {}", id);
		Optional<EducationLine> updatedEducationLine = repository.updateEducationLineName(new EducationLineId(id), new EducationLineName(request.name()));
		
		if (updatedEducationLine.isEmpty()) {
			log.warn("Education line not found: {}", id);
			throw new NotFoundException("educationline.not.found", Map.of("id", id));
		}

		EducationLineResponse response = new EducationLineResponse(
				updatedEducationLine.get().getId().value(),
				updatedEducationLine.get().getName().value(),
				updatedEducationLine.get().getDuration().years(),
				updatedEducationLine.get().getDuration().months(),
				updatedEducationLine.get().getDuration().days(),
				updatedEducationLine.get().getEducationRef().value(),
				updatedEducationLine.get().getCreatedAt().value(),
				updatedEducationLine.get().isActive()
		);
		log.info("Updated education line name: {}", response);
		return response;
	}

	@Override
	@Transactional
	public EducationLineResponse updateEducationLineDuration(UUID id, UpdateEducationLineDurationCmd request) {
		log.info("Updating education line duration for id: {}", id);
		Optional<EducationLine> updatedEducationLine = repository.updateEducationLineDuration(new EducationLineId(id), new EducationLineDuration(request.durationYears(), request.durationMonths(), request.durationDays()));
		
		if (updatedEducationLine.isEmpty()) {
			log.warn("Education line not found: {}", id);
			throw new NotFoundException("educationline.not.found", Map.of("id", id));
		}

		EducationLineResponse response = new EducationLineResponse(
				updatedEducationLine.get().getId().value(),
				updatedEducationLine.get().getName().value(),
				updatedEducationLine.get().getDuration().years(),
				updatedEducationLine.get().getDuration().months(),
				updatedEducationLine.get().getDuration().days(),
				updatedEducationLine.get().getEducationRef().value(),
				updatedEducationLine.get().getCreatedAt().value(),
				updatedEducationLine.get().isActive()
		);
		log.info("Updated education line name: {}", response);
		return response;
	}

	@Override
	@Transactional
	public EducationLineResponse deactivateEducationLine(UUID id) {
		log.info("Deactivating education line with id: {}", id);
		Optional<EducationLine> deactivatedEducationLine = repository.deactivate(new EducationLineId(id));
		
		if (deactivatedEducationLine.isEmpty()) {
			log.warn("Education line not found: {}", id);
			throw new NotFoundException("educationline.not.found", Map.of("id", id));
		}

		EducationLineResponse response = new EducationLineResponse(
				deactivatedEducationLine.get().getId().value(),
				deactivatedEducationLine.get().getName().value(),
				deactivatedEducationLine.get().getDuration().years(),
				deactivatedEducationLine.get().getDuration().months(),
				deactivatedEducationLine.get().getDuration().days(),
				deactivatedEducationLine.get().getEducationRef().value(),
				deactivatedEducationLine.get().getCreatedAt().value(),
				deactivatedEducationLine.get().isActive()
		);
		log.info("Deactivated education line: {}", response);
		return response;
	}

	@Override
	@Transactional
	public EducationLineResponse activateEducationLine(UUID id) {
		log.info("Activating education line with id: {}", id);
		Optional<EducationLine> activatedEducationLine = repository.activate(new EducationLineId(id));
		
		if (activatedEducationLine.isEmpty()) {
			log.warn("Education line not found: {}", id);
			throw new NotFoundException("educationline.not.found", Map.of("id", id));
		}
		
		EducationLineResponse response = new EducationLineResponse(
				activatedEducationLine.get().getId().value(),
				activatedEducationLine.get().getName().value(),
				activatedEducationLine.get().getDuration().years(),
				activatedEducationLine.get().getDuration().months(),
				activatedEducationLine.get().getDuration().days(),
				activatedEducationLine.get().getEducationRef().value(),
				activatedEducationLine.get().getCreatedAt().value(),
				activatedEducationLine.get().isActive()
		);
		log.info("Activated education line: {}", response);
		return response;
	}

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		log.info("Compensating education line with id: {}", clazz.getSimpleName());

		// Get latest state of the education line
		EducationLineId educationLineId = new EducationLineId(id);
		Optional<EducationLine> response = repository.findById(educationLineId);

		if (response == null || response.isEmpty()) {
			log.warn("Education line not found for compensation: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}

		boolean result = repository.compensate(educationLineId, sagaState);
		if (result) {
			log.info("Compensation successful for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}

	@Override
	public ResponseCompensated compensateActivate(UUID id, Class<?> clazz, SagaOutcome saga) {
		log.info("Compensating activate education line with id: {}", clazz.getSimpleName());

		EducationLineId educationLineId = new EducationLineId(id);
		Optional<EducationLine> response = repository.findById(educationLineId);

		if (response.isEmpty()) {
			log.warn("Education line not found for activate compensation: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}

		if (response.get().isActive()) {
			log.info("Activate compensation skipped (already active) for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateActivate(educationLineId, saga); 
		if (result) {
			log.info("Activate compensation successful for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}

	@Override
	public ResponseCompensated compensateDeactivate(UUID id, Class<?> clazz, SagaOutcome saga) {
		log.info("Compensating deactivate education line with id: {}", clazz.getSimpleName());

		EducationLineId educationLineId = new EducationLineId(id);
		Optional<EducationLine> response = repository.findById(educationLineId);

		if (response.isEmpty()) {
			log.warn("Education line not found for deactivate compensation: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}

		if (!response.get().isActive()) {
			log.info("Deactivate compensation skipped (already deactivated) for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateDeactivate(educationLineId, saga); 
		if (result) {
			log.info("Deactivate compensation successful for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}

	@Override
	public ResponseCompensated compensateName(UUID id, Class<?> clazz, SagaOutcome saga,
			UpdateEducationLineNameCmd nameReq) {
		log.info("Compensating education line name update with id: {}", clazz.getSimpleName());

		EducationLineId educationLineId = new EducationLineId(id);
		Optional<EducationLine> response = repository.findById(educationLineId);

		if (response.isEmpty()) {
			log.warn("Education line not found for name compensation: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}

		EducationLineName currentName = response.get().getName();
		EducationLineName targetName = new EducationLineName(nameReq.name());

		if (currentName.value().equals(targetName.value()))
		{
			log.info("Name compensation skipped (already in target state) for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateName(educationLineId, saga, targetName); 
		if (result) {
			log.info("Name compensation successful for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}

	@Override
	public ResponseCompensated compensateDuration(UUID id, Class<?> clazz, SagaOutcome saga,
			UpdateEducationLineDurationCmd req) {
		log.info("Compensating education line duration update with id: {}", clazz.getSimpleName());

		EducationLineId educationLineId = new EducationLineId(id);
		Optional<EducationLine> response = repository.findById(educationLineId);

		if (response.isEmpty()) { 
			log.warn("Education line not found for duration compensation: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}

		EducationLineDuration currentDuration = response.get().getDuration();
		EducationLineDuration duration = new EducationLineDuration(req.durationYears(), req.durationMonths(), req.durationDays());

		if (currentDuration.years() == duration.years()
				&& currentDuration.months() == duration.months()
				&& currentDuration.days() == duration.days()) {
			log.info("Duration compensation skipped (already in target state) for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
		}

		boolean result = repository.compensateDuration(educationLineId, saga, duration); 
		if (result) {
			log.info("Duration compensation successful for education line: {}", clazz.getSimpleName());
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}
}
