package local.sop.datawarehouse.consent.application.service;

import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.application.api.ConsentDirectory;
import local.sop.datawarehouse.consent.application.api.dto.*;
import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.datawarehouse.consent.domain.model.valueobject.PersonRef;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentRepositoryPort;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentStatementRepositoryPort;

import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ConsentApplicationService implements ConsentDirectory {
	private static final Logger log = LoggerFactory.getLogger(ConsentApplicationService.class);
    private final ConsentRepositoryPort consentRepositoryPort;
    private final ConsentStatementRepositoryPort consentStatementRepository;
 
    public ConsentApplicationService(
            ConsentRepositoryPort consentRepositoryPort,
            ConsentStatementRepositoryPort consentStatementRepository
    ) {
        this.consentRepositoryPort = consentRepositoryPort;
        this.consentStatementRepository = consentStatementRepository;
 
    }
 
    @Override
    public ConsentStatementResponse createConsentStatement(CreateConsentStatementCmd cmd) {
        var statement = consentStatementRepository.save(ConsentStatement.builder()
                .statementText(new ConsentStatementValue(cmd.statementText()))
                .active(cmd.active())
                .purpose(cmd.purpose())
                .type(cmd.type())
                .build()
        );
 
        log.info("Created consent statement: {}", statement);
 
        return toStatementResponse(statement);
    }
 
 
    @Transactional
    @Override
    public ConsentResponse grantConsent(GrantConsentCmd cmd) {
 
        var personRef = PersonRef.of(cmd.personRef());
        var statementRef = ConsentStatementRef.of(cmd.consentStatementRef());
        
        // Find statement
        var consentStatement = consentStatementRepository.findById(statementRef)
                .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", cmd.consentStatementRef())));
        
        // Aktivér statement om nødvendigt
        if(!consentStatement.isActive()) {
                consentStatement = consentStatement.withActive(true);
        }
        
        // Opret consent — status only; purpose/type come from the statement, not the grant
        var consent = Consent.builder()
                .personRef(personRef)
                .consentStatementRef(statementRef)
                .status(cmd.status())
                .build();
 
        // Add consent to statement
        Set<Consent> allConsents = new HashSet<>(consentStatement.getConsents());
        allConsents.add(consent);
        consentStatement = consentStatement.withConsents(allConsents);
 
        // Save statement with the new consent
        consentStatementRepository.save(consentStatement);
 
        log.info("Added consent to statement: {}", consentStatement.toString());
 
        // Save consent
        var savedConsent = consentRepositoryPort.save(consent);
        log.info("Granted consent: {}", savedConsent);
 
        return toConsentResponse(savedConsent, consentStatement);
    }
 
    @Transactional(readOnly = true)
    @Override
    public Optional<ConsentResponse> getConsent(UUID id) {
        Consent consent = consentRepositoryPort.findById(ConsentId.of(id))
                .orElseThrow(() -> new NotFoundException("consent.notfound", Map.of("consentId", id)));
 
        var statement = consentStatementRepository.findById(ConsentStatementRef.of(consent.getConsentStatementRef().value()))
                .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", consent.getConsentStatementRef())));
 
        return Optional.ofNullable(toConsentResponse(consent, statement));
    }
 
    @Transactional(readOnly = true)
    @Override
    public Optional<ConsentResponse> getConsentForPersonAndPurpose(FetchConsentForPersonAndPurposeQuery query) {
        Consent consent = consentRepositoryPort.findByPersonAndPurpose(PersonRef.of(query.personId()), query.purpose())
                .orElseThrow(() -> new NotFoundException("consent.notfound", Map.of("personId", query.personId(), "purpose", query.purpose())));
 
        var statement = consentStatementRepository.findById(ConsentStatementRef.of(consent.getConsentStatementRef().value()))
                .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", consent.getConsentStatementRef())));
 
        return Optional.ofNullable(toConsentResponse(consent, statement));
    }
 
    @Transactional
    @Override
    public ConsentResponse withdrawConsent(RevokeConsentCmd cmd) {
        var consent = consentRepositoryPort.findById(ConsentId.of(cmd.consentId()))
                .orElseThrow(() -> new NotFoundException("consent.notfound", Map.of("consentId", cmd.consentId()
                )));
 
        consent = consent.withStatus(ConsentStatus.WITHDRAWN);
        var updatedConsent = consentRepositoryPort.update(consent);
 
        log.info("Withdrawn consent: {}", updatedConsent);
 
        var statement = consentStatementRepository.findById(ConsentStatementRef.of(updatedConsent.getConsentStatementRef().value()))
                .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", updatedConsent.getConsentStatementRef())));
 
        return toConsentResponse(updatedConsent, statement);
    }
 
    @Transactional
    @Override
    public ConsentStatementResponse updateConsentStatement(UpdateConsentStatementCmd cmd) {
        var statement = consentStatementRepository.findById(ConsentStatementRef.of(cmd.consentStatementId()))
                .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementId", cmd.consentStatementId())));
 
        // CHANGED: purpose/type now actually passed through (previously
        // dead fields on the cmd). Both nullable in updateStatement —
        // null leaves the existing value unchanged.
        var updatedStatement = consentStatementRepository.updateStatement(
                statement.getId(), statement.isActive(), cmd.statementText(), cmd.purpose(), cmd.type());
 
        log.info("Updated consent statement: {}", updatedStatement);
 
        return toStatementResponse(updatedStatement);
    }
 
    @Transactional(readOnly = true)
    @Override
    public Optional<ConsentStatementResponse> getConsentStatement(FetchConsentStatementQuery query) {
        var statement = consentStatementRepository.findById(ConsentStatementRef.of(query.consentStatementId()))
                .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementId", query.consentStatementId())));
 
        return Optional.ofNullable(toStatementResponse(statement));
    }
 
    @Transactional(readOnly = true)
    @Override
    public List<ConsentResponse> getAllConsents() {
        return consentRepositoryPort.findAll().stream()
                .map(consent -> {
                    var statement = consentStatementRepository.findById(ConsentStatementRef.of(consent.getConsentStatementRef().value()))
                            .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", consent.getConsentStatementRef())));
                    return toConsentResponse(consent, statement);
                })
                .toList();
    }
 
    @Transactional(readOnly = true)
    @Override
    public List<ConsentStatementResponse> getAllConsentStatements() {
        return consentStatementRepository.findAll().stream()
                .map(this::toStatementResponse)
                .toList();
    }
 
    @Transactional(readOnly = true)
    @Override
    public List<ConsentResponse> getAllConsentsForPerson(FetchAllConsentsForPersonQuery query) {
        return consentRepositoryPort.findByPersonReference(PersonRef.of(query.personRef())).stream()
                .map(consent -> {
                    var statement = consentStatementRepository.findById(ConsentStatementRef.of(consent.getConsentStatementRef().value()))
                            .orElseThrow(() -> new NotFoundException("consentstatement.notfound", Map.of("statementRef", consent.getConsentStatementRef())));
                    return toConsentResponse(consent, statement);
                })
                .toList();
    }
 
    // Builds a ConsentResponse pulling status from the Consent and
    // purpose/type from the ConsentStatement it references — the point
    // of this whole change: those two attributes no longer live on the
    // same object.
    private ConsentResponse toConsentResponse(Consent consent, ConsentStatement statement) {
        return new ConsentResponse(
                consent.getId().value(),
                consent.getPersonRef().value(),
                consent.getStatus().name(),
                consent.getConsentStatementRef().value(),
                statement.getStatementText(),
                statement.getPurpose().name(),
                statement.getType().name(),
                statement.isActive()
        );
    }
 
    private ConsentStatementResponse toStatementResponse(ConsentStatement statement) {
        return new ConsentStatementResponse(
                statement.getId().value(),
                statement.getStatementText(),
                statement.isActive(),
                statement.getPurpose().name(),
                statement.getType().name()
        );
    }
 
 
    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        var statement = consentStatementRepository.findById(ConsentStatementRef.of(id));
        if(statement == null) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        boolean result = consentStatementRepository.compensate(ConsentStatementRef.of(id), sagaState);
        if(result) {
                return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
   }
 
    @Override
    public ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        var consent = consentRepositoryPort.findById(ConsentId.of(id));
        if(consent == null) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        boolean result = consentRepositoryPort.compensate(ConsentId.of(id), sagaState);
        if(result) {
                return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
    }
 
    @Override
    @Transactional
    public ResponseCompensated compensateConsentWithdrawalUpdate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        var consentOpt = consentRepositoryPort.findById(ConsentId.of(id));
        if (consentOpt.isEmpty()) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
 
        var consent = consentOpt.get();
 
        if (consent.getStatus() != ConsentStatus.WITHDRAWN) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
        }
 
        var restored = consent.withStatus(ConsentStatus.ACTIVE);
        consentRepositoryPort.update(restored);
 
        return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
    }
 
}