package local.sop.datawarehouse.apprentice.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.apprentice.application.api.ApprenticeDirectory;
import local.sop.datawarehouse.apprentice.application.api.dto.ApprenticeResponse;
import local.sop.datawarehouse.apprentice.application.api.dto.CreateApprenticeCmd;
import local.sop.datawarehouse.apprentice.application.api.dto.CreatedApprenticeResponse;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.*;
import local.sop.datawarehouse.apprentice.domain.ports.out.ApprenticeRepositoryPort;
import local.sop.datawarehouse.apprentice.domain.service.ApprenticeDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ApprenticeApplicationService implements ApprenticeDirectory {

    private final ApprenticeRepositoryPort apprentices;
    private final ApprenticeDomain domain;
    private static final Logger log = LoggerFactory.getLogger(ApprenticeApplicationService.class);

    ApprenticeApplicationService(ApprenticeRepositoryPort apprentices, ApprenticeDomain domain) {
        this.apprentices = apprentices;
        this.domain = domain;
    }

    @Override
    public CreatedApprenticeResponse createApprentice (CreateApprenticeCmd cmd) {

        try {
            var whs = domain.create(ApprenticeId.newId(), new PersonRef(cmd.personRef()), new EducationLineRef(cmd.educationLineRef()));
            whs = apprentices.save(whs);
            log.info("Apprentice created {}", whs);
            return new CreatedApprenticeResponse(whs.getApprenticeId().value());
        }
        catch(ValidationException ex) {
            log.warn("Error in create", ex.getMessage());
            throw new ValidationException("apprentice.create.failed", Map.of("function", "create"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApprenticeResponse> findById(UUID id) {
        if(id == null) throw new ValidationException("id.required", Map.of("function", "findById"));
        return apprentices.findById(id)
                .map(a -> new ApprenticeResponse(
                        a.getApprenticeId().value(),
                        a.getPersonRef().value(),
                        a.getEducationLineRef().value()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprenticeResponse> findByEducationLineId(UUID educationLineId) {
        if (educationLineId == null)
            throw new ValidationException("education_line_id.required", Map.of("function", "findByEducationLineId"));

        return apprentices.findByEducationLineId(educationLineId)
                        .stream()
                        .map(a -> new ApprenticeResponse(
                            a.getApprenticeId().value(),
                            a.getPersonRef().value(),
                            a.getEducationLineRef().value()
                        ))
                        .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprenticeResponse> findAll() {
        return apprentices.findAll()
                .stream()
                .map(a -> new ApprenticeResponse(
                        a.getApprenticeId().value(),
                        a.getPersonRef().value(),
                        a.getEducationLineRef().value()
                ))
                .toList();
    }

     @Override
    public ResponseCompensated compensate (UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        var apprentice = apprentices.findById(id);
        if(apprentice == null) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        boolean result = apprentices.compensate(ApprenticeId.of(id), sagaState);
        if(result) {
                return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}
}