package local.sop.sopinfo.auditlog.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.auditlog.application.api.AuditlogDirectory;
import local.sop.sopinfo.auditlog.application.api.dto.AuditlogResponse;
import local.sop.sopinfo.auditlog.application.api.dto.CreateAuditlogCmd;
import local.sop.sopinfo.auditlog.application.api.dto.CreatedAuditlogResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.auditlog.domain.service.AuditLogDomain;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.auditlog.domain.model.Log;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.*;
import local.sop.sopinfo.auditlog.domain.ports.out.AuditlogRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class AuditlogApplicationService implements AuditlogDirectory {
	private Logger log = LoggerFactory.getLogger(AuditlogApplicationService.class);
	private final AuditLogDomain AuditLogDomain;
	private final AuditlogRepositoryPort repository;
	public AuditlogApplicationService( AuditLogDomain AuditLogDomain , @Qualifier("JpaAuditlogRepository") AuditlogRepositoryPort repository) {
		this.AuditLogDomain = AuditLogDomain;
		this.repository = repository;
	}

	@Override
	@Transactional
	public CreatedAuditlogResponse createAuditlog(CreateAuditlogCmd cmd) {
		ActorType actorType = cmd.actorType();
		ActorRef actorRef = new ActorRef(cmd.actorRef());
		Severity severity = cmd.severity();
		OriginSystem originSystem = new OriginSystem(cmd.originSystem());
		OriginService originService = new OriginService(cmd.originService());
		OriginComponent originComponent = new OriginComponent(cmd.originComponent());
	

		Data data = new Data(cmd.data());
		Description description = new Description(cmd.description());

		Log draftLog = Log.builder()
				.actorRef(actorRef)
				.actorType(actorType)
				.severity(severity)
				.originSystem(originSystem)
				.originService(originService)
				.originComponent(originComponent)
				.data(data)
				.description(description)
				.timestamp(LogTimestamp.now())
				.build();


		Log created = AuditLogDomain.createLog(draftLog);
		Log saved = repository.save(created);
		return new CreatedAuditlogResponse(saved.getId().value());
	}
	@Override
	@Transactional(readOnly = true)
	public List<AuditlogResponse> findAll() {
		List<Log> logs = repository.findAll();
		return logs.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<AuditlogResponse> findBySearchParams(
			UUID id,
			UUID actorRef,
			ActorType actorType,
			Severity severity,
			String originSystem,
			String originService,
			String originComponent
	) {
		List<Log> logs = repository.findBySearchParams(
				id, actorRef, actorType, originSystem, originService, originComponent, severity
		);

		return logs.stream()
				.map(this::toResponse)
				.toList();
	}


	private AuditlogResponse toResponse(Log log) {
		return new AuditlogResponse(
				log.getId().value(),
				log.getActorRef().value(),
				log.getActorType(),
				log.getSeverity(),
				log.getOriginSystem().value(),
				log.getOriginService().value(),
				log.getOriginComponent().value(),
				log.getData().json(),
				log.getDescription() == null ? null : log.getDescription().value(),
				log.getTimestamp().value()
		);
	}

	@Override
	public Optional<AuditlogResponse> findById(UUID id) {
		Optional<Log> logs = repository.findById(id);
		return logs.map(this::toResponse);
	}

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		log.info("Compensate called from class {}", clazz);
		var al = repository.findById(id);
		if(al == null) {
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}
		boolean result = repository.compensate(LogId.of(id), sagaState);
		if(result) {
			return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}
}
