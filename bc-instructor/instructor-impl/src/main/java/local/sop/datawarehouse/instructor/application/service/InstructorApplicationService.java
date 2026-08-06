package local.sop.datawarehouse.instructor.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.instructor.application.api.InstructorDirectory;
import local.sop.datawarehouse.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.datawarehouse.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.datawarehouse.instructor.application.api.dto.InstructorResponse;
import local.sop.datawarehouse.instructor.domain.model.Instructor;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.InstructorId;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.instructor.domain.ports.out.InstructorRepositoryPort;

@Service
public class InstructorApplicationService implements InstructorDirectory {

    private static final Logger log = LoggerFactory.getLogger(InstructorApplicationService.class);

    private final InstructorRepositoryPort repository;

    public InstructorApplicationService(
        InstructorRepositoryPort repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CreatedInstructorResponse createInstructor(CreateInstructorCmd cmd) {
        log.info("Creating instructor with personRef={}", cmd.personRef());

        PersonRef personRef = PersonRef.of(cmd.personRef());
        Instructor instructor = Instructor.create(personRef);

        Instructor saved = repository.save(instructor);

        log.info("Instructor saved with id={}", saved.getId().value());

        return new CreatedInstructorResponse(saved.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorResponse> findAll() {
        log.debug("Fetching all instructors from repository");

        List<InstructorResponse> result = repository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();

        log.debug("Fetched {} instructors", result.size());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorResponse findById(UUID id) {
        log.debug("Fetching instructor with id={}", id);

        Instructor instructor = repository.findById(InstructorId.of(id))
                .orElseThrow(() -> {
                    log.warn("Instructor not found with id={}", id);
                    return new NotFoundException(
                            "instructor.notFound",
                            Map.of("id", id)
                    );
                });

        log.debug("Instructor found with id={}", id);

        return toResponse(instructor);
    }
    @Override
    public ResponseCompensated compensate (UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        var instructor = repository.findById(InstructorId.of(id));
        if(instructor.isEmpty()) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        boolean result = repository.compensate(InstructorId.of(id), sagaState);
        if(result) {
                return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}

    private InstructorResponse toResponse(Instructor instructor) {
    return new InstructorResponse(
            instructor.getId().value(),
            instructor.getPersonRef().value()
    );
    }
}