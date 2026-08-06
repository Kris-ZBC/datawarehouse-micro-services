package local.sop.datawarehouse.anonymize.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.anonymize.application.api.AnonymizeDirectory;
import local.sop.datawarehouse.anonymize.application.api.dto.AnonymizeResponse;
import local.sop.datawarehouse.anonymize.application.api.dto.CreateAnonymizeCmd;
import local.sop.datawarehouse.anonymize.application.api.dto.FetchByIdQuery;
import local.sop.datawarehouse.anonymize.application.api.dto.FetchByParamsQuery;
import local.sop.datawarehouse.anonymize.domain.model.Anonymize;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.anonymize.domain.ports.out.AnonymizeRepositoryPort;
import local.sop.datawarehouse.anonymize.domain.service.AnonymizeDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;

@Service
public class AnonymizeApplicationService implements AnonymizeDirectory {
    private static final Logger log = LoggerFactory.getLogger(AnonymizeApplicationService.class);
    private final AnonymizeDomain anonymizationDomain;
    private final AnonymizeRepositoryPort repository;

    public AnonymizeApplicationService(
            AnonymizeDomain anonymizationDomain,
            AnonymizeRepositoryPort repository
    ) {
        this.anonymizationDomain = anonymizationDomain;
        this.repository = repository;
    }

    @Override
    @Transactional
    public AnonymizeResponse create(CreateAnonymizeCmd cmd) {
        PersonRef personRef = PersonRef.of(cmd.personRef());

        Anonymize draft = anonymizationDomain.create(personRef);
        Anonymize saved = repository.save(draft);

        return new AnonymizeResponse(saved.getAnonymizationId().value(), saved.getPersonRef().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnonymizeResponse> findById(FetchByIdQuery query) {
        var result = repository.findById(AnonymizeId.of(query.id())).orElseThrow(() -> new NotFoundException("key.notfound", Map.of("anonymizationId", query.id())));
        return Optional.of(toResponse(result));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnonymizeResponse> findByParams(FetchByParamsQuery query) {
        UUID anonymizationId = query.anonymizationId();
        UUID personRef = query.personRef();
        return repository.findBySearchParams(anonymizationId != null ? AnonymizeId.of(anonymizationId) : null, personRef != null ? PersonRef.of(personRef) : null)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AnonymizeResponse toResponse(Anonymize anonymizeLog) {
        return new AnonymizeResponse(
                anonymizeLog.getAnonymizationId().value(),
                anonymizeLog.getPersonRef().value()
        );
    }

    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        boolean result = repository.compensate(AnonymizeId.of(id), sagaState);
        if (result) {
            return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
    }
}
