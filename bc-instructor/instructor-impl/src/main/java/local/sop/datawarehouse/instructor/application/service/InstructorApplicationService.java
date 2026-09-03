package local.sop.datawarehouse.instructor.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
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
import local.sop.datawarehouse.sharedlib.login.WellKnownLogins;

@Service
public class InstructorApplicationService implements InstructorDirectory {

    private static final Logger log = LoggerFactory.getLogger(InstructorApplicationService.class);

    private final InstructorRepositoryPort repository;

    public InstructorApplicationService(
        InstructorRepositoryPort repository
    ) {
        this.repository = repository;
    }

    /**
     * CHANGED: the "tech user may only create an instructor if none
     * already exists" rule used to live in a separate login-validation
     * module (LoginAspect / @ValidateLogin), calling out to bc-login
     * and bc-instructor via two dedicated interfaces (ValidateTechUser,
     * CheckInstructorExist) — plus that module's auto-configuration
     * never actually compiled, so the rule was never enforced anywhere.
     *
     * Moved here because this is a domain invariant about the
     * Instructor aggregate, not an access-control concern — it belongs
     * to the BC that owns the invariant, not the gateway or a separate
     * cross-cutting module. Both facts it needs are already local:
     * "does an instructor exist" is repository.exists() (already used
     * elsewhere in this class), and "is the caller the tech user" is a
     * pure comparison against the well-known ID — no new port, no new
     * interface, no cross-BC call at request time.
     *
     * callerLoginId is nullable because gw-admin doesn't yet resolve
     * the authenticated caller's identity from the session cookie (no
     * gateway-level session resolution exists yet) — until that's
     * built, this guard simply never matches (a null loginId can't
     * equal TECH_USER_ID), which is the same "not enforced yet" state
     * this rule has always actually been in, not a regression.
     */
    @Override
    @Transactional
    public CreatedInstructorResponse createInstructor(CreateInstructorCmd cmd) {
        log.info("Creating instructor with personRef={}", cmd.personRef());

        if (WellKnownLogins.TECH_USER_ID.equals(cmd.callerLoginId()) && repository.exists()) {
            log.warn("Rejected: tech user attempted to create an instructor while one already exists");
            throw new ConflictException("instructor.techuser.notallowed", Map.of("callerLoginId", cmd.callerLoginId()));
        }

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

    // NEW: supports login-saga's role resolution — "is this person an
    // instructor at all". Optional return, no throw-on-missing: not
    // being an instructor is a completely normal outcome here (the
    // person might be an apprentice instead), unlike findById() above
    // where a missing id genuinely is an error.
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorResponse> findByPersonRef(UUID personRef) {
        log.debug("Fetching instructor with personRef={}", personRef);
        return repository.findByPersonRef(personRef)
                .map(this::toResponse);
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