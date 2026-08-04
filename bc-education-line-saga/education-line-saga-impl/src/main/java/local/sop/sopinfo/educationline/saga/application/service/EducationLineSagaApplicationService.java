package local.sop.sopinfo.educationline.saga.application.service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import local.sop.sopinfo.educationline.saga.application.api.EducationLineSagaDirectory;
import local.sop.sopinfo.educationline.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.educationline.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.educationline.saga.application.ports.out.education.EducationPort;
import local.sop.sopinfo.educationline.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.sopinfo.educationline.saga.application.api.dto.*;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

@Service
public class EducationLineSagaApplicationService implements EducationLineSagaDirectory {

	private static Logger log = LoggerFactory.getLogger(EducationLineSagaApplicationService.class);

	private final EducationLinePort educationLinePort;
	private final EducationPort educationPort;
	private final AuditlogPort auditlogPort;

	public EducationLineSagaApplicationService(EducationLinePort educationLinePort, EducationPort educationPort,
			AuditlogPort auditlogPort) {
		this.educationLinePort = educationLinePort;
		this.educationPort = educationPort;
		this.auditlogPort = auditlogPort;
	}

	@Override
	public EducationLineResponse createEducationLine(CreateEducationLineCmd cmd) {
		// 1. get Education via educationRef to verify it exists, if not exist throw
		// exception to prevent creation of education line with invalid reference
		try {
			EducationResponse response = educationPort.existsById(cmd.educationRef());
			if (response == null) {
				log.warn("Education reference with id: {} does not exist for creating education line",
						cmd.educationRef());
				throw new ConflictException("educationline.create.failed.educationref.not.found",
						Map.of("id", cmd.educationRef()));
			}
		} catch (DomainException ex) {
			log.warn("Failed to verify education reference with id: {} for creating education line", cmd.educationRef(),
					ex);
			throw new ConflictException("educationline.create.failed.educationref.verification",
					Map.of("id", cmd.educationRef()));
		}

		UUID educationLineId;
		try {
			// 2 Create the education line
			educationLineId = educationLinePort.createEducationLine(cmd.name(), cmd.durationYears(),
					cmd.durationMonths(), cmd.durationDays(), cmd.educationRef());
		} catch (DomainException de) {
			throw de;
		} catch (RuntimeException e) {
			// 1.2 Failed to create throw exception
			log.warn("Failed to create an education line", cmd);
			throw new ConflictException("educationline.create.failed", Map.of("object", "educationLine"));
		}

		EducationLineResponse response;
		try {
			// 3.1 Check if education line is created successfully by retrieving it
			response = educationLinePort.findEducationLineById(educationLineId);
			if (response == null) {
				log.warn("Education line creation verification failed. Education line with id: {} not found.",
						educationLineId);
				throw new ConflictException("educationline.create.verification.failed", Map.of("id", educationLineId));
			}
		} catch (ConflictException ce) {
			compensateEducationLine(educationLineId);
			throw ce;
		} catch (DomainException de) {
			compensateEducationLine(educationLineId);
			throw de;
		} catch (RuntimeException e) {
			// 2.2 Failed to retrieve the created education line, trigger compensation and
			// throw exception
			compensateEducationLine(educationLineId);
			throw new ConflictException("educationline.retrieve.failed",
					Map.of("object", "educationLine", "id", educationLineId));
		}

		createAuditlogWithVerification(new CreateAuditlogCmd(
				cmd.actorRef(),
				cmd.actorType(),
				cmd.severity(),
				cmd.originSystem(),
				cmd.originService(),
				cmd.originComponent(),
				cmd.data(),
				cmd.description()),
				educationLineId,
				"the creation of education line with id: " + educationLineId,
				() -> compensateEducationLine(educationLineId));

		return response;
	}

	@Override
	public EducationLineResponse updateEducationLineName(UUID educationLineId, UpdateEducationLineNameCmd cmd) {
		// 0. Get previous name for compensation
		String previousName = null;
		try {
			EducationLineResponse previousResponse = educationLinePort.findEducationLineById(educationLineId);
			if (previousResponse != null) {
				previousName = previousResponse.name();
			}
		} catch (RuntimeException e) {
			log.warn("Failed to retrieve previous name for education line with id: {}", educationLineId);
		}

		// 1.1 Update the education line name
		try {
			educationLinePort.updateEducationLineName(educationLineId, cmd.name());

		} catch (DomainException de) {
			throw de;
		} catch (RuntimeException e) {
			// 1.2 Failed to update throw exception
			log.warn("Failed to update education line name for education line with id: {}", educationLineId, cmd);
			throw new ConflictException("educationline.update.name.failed", Map.of("id", educationLineId));
		}

		// 2.1 Check if education line name is updated successfully by retrieving it
		EducationLineResponse response;
		try {
			response = educationLinePort.findEducationLineById(educationLineId);
			if (response == null) {
				log.warn("Education line name update verification failed. Education line with id: {} not found.",
						educationLineId);
				throw new ConflictException("educationline.update.name.verification.failed",
						Map.of("id", educationLineId));
			}
			if (!Objects.equals(response.name(), cmd.name())) {
				log.warn(
						"Education line name update verification failed for education line with id: {}. Expected name: {}, Actual name: {}",
						educationLineId, cmd.name(), response.name());
				throw new ConflictException("educationline.update.name.verification.failed",
						Map.of("id", educationLineId));
			}
		} catch (ConflictException ce) {
			compensateUpdateName(educationLineId, previousName);
			throw ce;
		} catch (DomainException de) {
			compensateUpdateName(educationLineId, previousName);
			throw de;
		} catch (RuntimeException e) {
			// 2.2 Failed to retrieve the updated education line, trigger compensation and
			// throw exception
			compensateUpdateName(educationLineId, previousName);
			throw new ConflictException("educationline.retrieve.failed",
					Map.of("object", "educationLine", "id", educationLineId));
		}

		final String previousNameForAuditlogCompensation = previousName;

		createAuditlogWithVerification(new CreateAuditlogCmd(
				cmd.actorRef(),
				cmd.actorType(),
				cmd.severity(),
				cmd.originSystem(),
				cmd.originService(),
				cmd.originComponent(),
				cmd.data(),
				cmd.description()),
				educationLineId,
				"the update of education line name for education line with id: " + educationLineId,
				() -> compensateUpdateName(educationLineId, previousNameForAuditlogCompensation));

		return response;
	}

	@Override
	public EducationLineResponse updateEducationLineDuration(UUID educationLineId, UpdateEducationLineDurationCmd cmd) {
		// 0. Get previous duration for compensation
		int previousDurationYears = 0;
		int previousDurationMonths = 0;
		int previousDurationDays = 0;
		try {
			EducationLineResponse previousResponse = educationLinePort.findEducationLineById(educationLineId);
			if (previousResponse != null) {
				previousDurationYears = previousResponse.durationYears();
				previousDurationMonths = previousResponse.durationMonths();
				previousDurationDays = previousResponse.durationDays();
			}
		} catch (RuntimeException e) {
			log.warn("Failed to retrieve previous duration for education line with id: {}", educationLineId);
		}

		// 1.1 Update duration of the education line
		try {
			educationLinePort.updateEducationLineDuration(educationLineId, cmd.durationYears(), cmd.durationMonths(),
					cmd.durationDays());
		} catch (DomainException de) {
			throw de;
		} catch (RuntimeException e) {
			// 1.2 Failed to update throw exception
			log.warn("Failed to update education line duration for education line with id: {}", educationLineId, cmd);
			throw new ConflictException("educationline.update.duration.failed", Map.of("id", educationLineId));
		}

		// 2.1 Check if education line duration is updated successfully by retrieving it
		EducationLineResponse response;
		try {
			response = educationLinePort.findEducationLineById(educationLineId);
			if (response == null) {
				log.warn("Education line duration update verification failed. Education line with id: {} not found.",
						educationLineId);
				throw new ConflictException("educationline.update.duration.verification.failed",
						Map.of("id", educationLineId));
			}
			if (!Objects.equals(response.durationYears(), cmd.durationYears())
					|| !Objects.equals(response.durationMonths(), cmd.durationMonths())
					|| !Objects.equals(response.durationDays(), cmd.durationDays())) {
				log.warn(
						"Education line duration update verification failed for education line with id: {}. Expected duration: {}, Actual duration: {}",
						educationLineId, cmd, response);
				throw new ConflictException("educationline.update.duration.verification.failed",
						Map.of("id", educationLineId));
			}
		} catch (ConflictException ce) {
			compensateUpdateDuration(educationLineId, previousDurationYears, previousDurationMonths, previousDurationDays);
			throw ce;
		} catch (DomainException de) {
			compensateUpdateDuration(educationLineId, previousDurationYears, previousDurationMonths, previousDurationDays);
			throw de;
		} catch (RuntimeException e) {
			// 2.2 Failed to retrieve the updated education line, trigger compensation and
			// throw exception
			compensateUpdateDuration(educationLineId, previousDurationYears, previousDurationMonths, previousDurationDays);
			throw new ConflictException("educationline.retrieve.failed",
					Map.of("object", "educationLine", "id", educationLineId));
		}

		final int previousDurationYearsForAuditlogCompensation = previousDurationYears;
		final int previousDurationMonthsForAuditlogCompensation = previousDurationMonths;
		final int previousDurationDaysForAuditlogCompensation = previousDurationDays;

		createAuditlogWithVerification(new CreateAuditlogCmd(
				cmd.actorRef(),
				cmd.actorType(),
				cmd.severity(),
				cmd.originSystem(),
				cmd.originService(),
				cmd.originComponent(),
				cmd.data(),
				cmd.description()),
				educationLineId,
				"the update of education line duration for education line with id: " + educationLineId,
				() -> compensateUpdateDuration(educationLineId, previousDurationYearsForAuditlogCompensation,
						previousDurationMonthsForAuditlogCompensation,
						previousDurationDaysForAuditlogCompensation));

		return response;
	}

	@Override
	public EducationLineResponse deactivateEducationLine(UUID educationLineId, CreateAuditlogCmd cmd) {
		// 1.1 Deactivate the education line
		try {
			educationLinePort.deactivateEducationLine(educationLineId);
			log.info("Successfully deactivated education line with id: {}", educationLineId);
		} catch (DomainException de) {
			throw de;
		} catch (RuntimeException e) {
			// 1.2 Failed to deactivate throw exception
			log.warn("Failed to deactivate education line with id: {}", educationLineId);
			throw new ConflictException("educationline.deactivate.failed", Map.of("id", educationLineId));
		}

		// 2.1 Check if education line is deactivated successfully by retrieving it
		EducationLineResponse response;
		try {
			response = educationLinePort.findEducationLineById(educationLineId);
			if (response == null) {
				log.warn("Education line deactivation verification failed. Education line with id: {} not found.",
						educationLineId);
				throw new ConflictException("educationline.deactivate.verification.failed",
						Map.of("id", educationLineId));
			}
			if (response.isActive()) {
				log.warn(
						"Education line deactivation verification failed for education line with id: {}. Expected active: false, Actual active: {}",
						educationLineId, response.isActive());
				throw new ConflictException("educationline.deactivate.verification.failed",
						Map.of("id", educationLineId));
			}
		} catch (ConflictException ce) {
			compensateDeactivate(educationLineId);
			throw ce;
		} catch (DomainException de) {
			compensateDeactivate(educationLineId);
			throw de;
		} catch (RuntimeException e) {
			// 2.2 Failed to retrieve the updated education line, trigger compensation and
			// throw exception
			compensateDeactivate(educationLineId);
			throw new ConflictException("educationline.retrieve.failed",
					Map.of("object", "educationLine", "id", educationLineId));
		}

		createAuditlogWithVerification(cmd, educationLineId, "the deactivation of education line with id: " + educationLineId,
				() -> compensateDeactivate(educationLineId));
		return response;
	}

	public EducationLineResponse activateEducationLine(UUID educationLineId, CreateAuditlogCmd cmd) {
		// 1.1 Activate the education line
		try {
			educationLinePort.activateEducationLine(educationLineId);
			log.info("Successfully activated education line with id: {}", educationLineId);
		} catch (DomainException de) {
			throw de;
		} catch (RuntimeException e) {
			// 1.2 Failed to activate throw exception
			log.warn("Failed to activate education line with id: {}", educationLineId);
			throw new ConflictException("educationline.activate.failed", Map.of("id", educationLineId));
		}

		// 2.1 Check if education line is activated successfully by retrieving it
		EducationLineResponse response;
		try {
			response = educationLinePort.findEducationLineById(educationLineId);
			if (response == null) {
				log.warn("Education line activation verification failed. Education line with id: {} not found.",
						educationLineId);
				throw new ConflictException("educationline.activate.verification.failed",
						Map.of("id", educationLineId));
			}
			if (!response.isActive()) {
				log.warn(
						"Education line activation verification failed for education line with id: {}. Expected active: true, Actual active: {}",
						educationLineId, response.isActive());
				throw new ConflictException("educationline.activate.verification.failed",
						Map.of("id", educationLineId));
			}
		} catch (ConflictException ce) {
			compensateActivate(educationLineId);
			throw ce;
		} catch (DomainException de) {
			compensateActivate(educationLineId);
			throw de;
		} catch (RuntimeException e) {
			// 2.2 Failed to retrieve the updated education line, trigger compensation and
			// throw exception
			compensateActivate(educationLineId);
			throw new ConflictException("educationline.retrieve.failed",
					Map.of("object", "educationLine", "id", educationLineId));
		}

		createAuditlogWithVerification(cmd, educationLineId, "the activation of education line with id: " + educationLineId,
				() -> compensateActivate(educationLineId));

		return response;
	}

	// Create auditlog with sanity check
	/* ===== Runnable is just a functional interface so i can pass a compensation action ===== */
	private void createAuditlogWithVerification(CreateAuditlogCmd cmd, UUID educationLineId, String actionDescription,
			Runnable compensationAction) {
		UUID auditlogId = createAuditlog(cmd, educationLineId, actionDescription, compensationAction);
		verifyAuditlogCreated(auditlogId, compensationAction);
	}

	private UUID createAuditlog(CreateAuditlogCmd cmd, UUID educationLineId, String actionDescription,
			Runnable compensationAction) {
		UUID auditlogId = null;
		try {
			auditlogId = auditlogPort.create(
					cmd.actorRef(),
					cmd.actorType(),
					cmd.severity(),
					cmd.originSystem(),
					cmd.originService(),
					cmd.originComponent(),
					cmd.data(),
					cmd.description());
			log.info("Successfully created an audit log with id: {} for {}", auditlogId, actionDescription);
		} catch (DomainException de) {
			log.warn("Failed to create an audit log for {}", actionDescription, de);
			if (auditlogId != null) {
				compensateAuditlog(auditlogId);
			}
			compensationAction.run();
			throw de;
		} catch (RuntimeException e) {
			log.warn("Failed to create an audit log for {}", actionDescription, e);
			if (auditlogId != null) {
				compensateAuditlog(auditlogId);
			}
			compensationAction.run();
			throw new ConflictException("auditlog.create.failed", Map.of("object", "auditlog"));
		}

		return auditlogId;
	}

	// Auditlog sanity check
	private void verifyAuditlogCreated(UUID auditlogId, Runnable compensationAction) {
		UUID auditlogIdCheck;
		try {
			auditlogIdCheck = auditlogPort.findById(auditlogId);
		} catch (DomainException de) {
			log.warn("Failed to perform sanity check for audit log with id: {}", auditlogId, de);
			compensateAuditlog(auditlogId);
			compensationAction.run();
			throw de;
		} catch (RuntimeException re) {
			compensateAuditlog(auditlogId);
			compensationAction.run();
			throw new ConflictException("auditlog.retrieve.failed", Map.of("id", auditlogId.toString()));
		}

		if (auditlogIdCheck == null) {
			log.warn("Sanity check failed: Created audit log with id: {} cannot be found", auditlogId);
			compensateAuditlog(auditlogId);
			compensationAction.run();
			throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
		}
	}

	private void compensateAuditlog(UUID auditlogId) {
		if (auditlogId == null) {
			return;
		}
		try {
			ResponseCompensated result = auditlogPort.compensate(auditlogId, this.getClass(),
					SagaOutcome.COMPENSATED);
			logCompensationResult("audit log", auditlogId, result);
		} catch (DomainException compensateEx) {
			log.warn("Failed to compensate audit log with id: {}", auditlogId, compensateEx);
		}
	}

	private void compensateEducationLine(UUID educationLineId) {
		try {
			ResponseCompensated result = educationLinePort.compensate(educationLineId, this.getClass(),
					SagaOutcome.COMPENSATED);
			logCompensationResult("education line", educationLineId, result);
		} catch (DomainException compensateEx) {
			log.warn("Failed to compensate education line with id: {}", educationLineId, compensateEx);
		}
	}

	private void logCompensationResult(String entityName, UUID entityId, ResponseCompensated result) {
		if (result != null) {
			log.warn("Compensated {} with id: {}, Success: {}", entityName, entityId, result.success());
		}
	}

	private void compensateUpdateName(UUID educationLineId, String previousName) {
		if (previousName == null) {
			return;
		}
		try {
			ResponseCompensated result = educationLinePort.compensateUpdateName(educationLineId, this.getClass(),
					SagaOutcome.COMPENSATED, previousName);
			logCompensationResult("education line name update", educationLineId, result);
		} catch (DomainException compensateEx) {
			log.warn("Failed to compensate education line name update with id: {}", educationLineId, compensateEx);
		}
	}

	private void compensateUpdateDuration(UUID educationLineId, int previousDurationYears,
			int previousDurationMonths, int previousDurationDays) {
		try {
			ResponseCompensated result = educationLinePort.compensateUpdateDuration(educationLineId, this.getClass(),
					SagaOutcome.COMPENSATED, previousDurationYears, previousDurationMonths, previousDurationDays);
			logCompensationResult("education line duration update", educationLineId, result);
		} catch (DomainException compensateEx) {
			log.warn("Failed to compensate education line duration update with id: {}", educationLineId, compensateEx);
		}
	}

	private void compensateActivate(UUID educationLineId) {
		try {
			ResponseCompensated result = educationLinePort.compensateActivate(educationLineId, this.getClass(),
					SagaOutcome.COMPENSATED);
			logCompensationResult("education line activate", educationLineId, result);
		} catch (DomainException compensateEx) {
			log.warn("Failed to compensate education line activate with id: {}", educationLineId, compensateEx);
		}
	}

	private void compensateDeactivate(UUID educationLineId) {
		try {
			ResponseCompensated result = educationLinePort.compensateDeactivate(educationLineId, this.getClass(),
					SagaOutcome.COMPENSATED);
			logCompensationResult("education line deactivate", educationLineId, result);
		} catch (DomainException compensateEx) {
			log.warn("Failed to compensate education line deactivate with id: {}", educationLineId, compensateEx);
		}
	}
}
