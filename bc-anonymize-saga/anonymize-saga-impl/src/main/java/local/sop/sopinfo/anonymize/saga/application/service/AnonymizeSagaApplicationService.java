package local.sop.sopinfo.anonymize.saga.application.service;

import java.util.Map;
import java.util.UUID;

import local.sop.sopinfo.anonymize.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.person.application.api.dto.PersonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import local.sop.sopinfo.anonymize.saga.application.ports.out.anonymize.AnonymizePort;
import local.sop.sopinfo.anonymize.saga.application.ports.out.person.PersonPort;
import local.sop.sopinfo.anonymize.saga.application.api.dto.AnonymizeResponse;
import local.sop.sopinfo.anonymize.saga.application.api.dto.CreateAnonymizeCmd;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.anonymize.saga.application.api.AnonymizeSagaDirectory;


@Service
public class AnonymizeSagaApplicationService implements AnonymizeSagaDirectory {

	private final AnonymizePort anonymizePort;
	private final PersonPort personPort;
    private final AuditlogPort auditlogPort;

	private static final Logger logger = LoggerFactory.getLogger(AnonymizeSagaApplicationService.class);

	public AnonymizeSagaApplicationService(AnonymizePort anonymizePort, PersonPort personPort,  AuditlogPort auditlogPort) {
		this.anonymizePort = anonymizePort;
		this.personPort = personPort;
        this.auditlogPort = auditlogPort;
	}

	/*
	* 1. create anonymize with personRef
	  * 1.1 success
	  * 1.2 failure -> throw exception
    * 2. get person (sanity check)
	  * 2.1 success
	  * 2.2 failure -> compensate anonymize -> throw exception
    * 3. create auditlog
      * 3.1 success -> return ok
      * 3.2 failure -> compensate auditlog (if created) -> compensate anonymize -> throw exception
	*/
	public AnonymizeResponse create(CreateAnonymizeCmd cmd) {
		// 1. create anonymize with personRef
		UUID anonymizationId;
		try {
			anonymizationId = anonymizePort.create(cmd.personRef());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			logger.warn("SAGA: anonymize create failed for personRef {}", cmd.personRef());
			throw new ConflictException("anonymize.notcreated", Map.of("object", "anonymization"));
		}

        // 2. get person (sanity check)
        PersonResponse person;
		try {
            person = personPort.get(cmd.personRef());
			if (person == null) {
				var result = anonymizePort.compensate(anonymizationId, this.getClass(), SagaOutcome.COMPENSATED);
				logger.warn("SAGA: person get returned null, compensated {}. Success: {}", anonymizationId, result.success());
				throw new ConflictException("person.not.found", Map.of("id", cmd.personRef().toString()));
			}
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			logger.warn("SAGA: person get failed for id {}, compensating anonymization {}", cmd.personRef(), anonymizationId);
			var result = anonymizePort.compensate(anonymizationId, this.getClass(), SagaOutcome.COMPENSATED);
			logger.warn("SAGA: anonymization compensated {}. Success: {}", anonymizationId, result.success());
			throw new ConflictException("person.read_failed", Map.of("id", cmd.personRef().toString()));
		}

		// 3. Create auditlog
		UUID auditlogId = null;
		try {
			auditlogId = auditlogPort.create(
				cmd.actorRef(), cmd.actorType(), cmd.severity(),
				cmd.originSystem(), cmd.originService(), cmd.originComponent(),
				cmd.data(), cmd.description()
			);
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			logger.warn("SAGA: auditlog create failed, compensating. AnonymizationId: {}", anonymizationId);
			if (auditlogId != null) {
				var auditlogResult = auditlogPort.compensate(auditlogId, this.getClass(), SagaOutcome.COMPENSATED);
				logger.warn("SAGA: auditlog compensated {}. Success: {}", auditlogId, auditlogResult.success());
			}
			var anonymizeResult = anonymizePort.compensate(anonymizationId, this.getClass(), SagaOutcome.COMPENSATED);
			logger.warn("SAGA: anonymization compensated {}. Success: {}", anonymizationId, anonymizeResult.success());
			throw new ConflictException("auditlog.notcreated", Map.of("object", "auditlog"));
		}

		return new AnonymizeResponse(anonymizationId, cmd.personRef());
	}
}
