package local.sop.datawarehouse.education.saga.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import local.sop.datawarehouse.education.saga.application.api.EducationSagaDirectory;
import local.sop.datawarehouse.education.saga.application.api.dto.*;
import local.sop.datawarehouse.education.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.education.saga.application.ports.out.education.EducationPort;
import local.sop.datawarehouse.education.saga.application.ports.out.educationinstructor.EducationInstructorPort;
import local.sop.datawarehouse.education.saga.application.ports.out.saga.EducationSagaStatePort;

@Service
public class EducationSagaApplicationService implements EducationSagaDirectory {

    private static final Logger log = LoggerFactory.getLogger(EducationSagaApplicationService.class);

    private final EducationPort educationPort;
    private final EducationInstructorPort educationInstructorPort;
    private final AuditlogPort auditlogPort;
    private final EducationSagaStatePort sagaLock;

    public EducationSagaApplicationService(
            EducationPort educationPort,
            EducationInstructorPort educationInstructorPort,
            AuditlogPort auditlogPort,
            EducationSagaStatePort sagaLock) {

        this.educationPort = educationPort;
        this.educationInstructorPort = educationInstructorPort;
        this.auditlogPort = auditlogPort;
        this.sagaLock = sagaLock;
    }

    // =========================================================
    // LOCK HELPERS (SAFE)
    // =========================================================

    private void acquireLock(UUID sessionId) {
        if (!sagaLock.tryLock(SagaConcurrencyLock.start(sessionId))) {
            throw new ConflictException("saga.already.running",
                    Map.of("sessionId", sessionId.toString()));
        }
    }

    private void releaseLock(UUID sessionId) {
        sagaLock.release(sessionId);
    }

    private void compensate(UUID sessionId) {
        sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
    }

    // =========================================================
    // VALIDATION HELPERS (NO SIDE EFFECTS)
    // =========================================================

    private EducationResponse validateEducation(UUID educationId) {
        EducationResponse edu = educationPort.findEducationById(educationId);
        if (edu == null) {
            throw new ConflictException("education.not.found",
                    Map.of("id", educationId.toString()));
        }
        return edu;
    }

    private void validateEducationInstructor(CompositeKey id) {
        if (educationInstructorPort.findById(id).isEmpty()) {
            throw new ConflictException("educationinstructor.not.found",
                    Map.of("id", id.toString()));
        }
    }

    // =========================================================
    // AUDIT HELPERS
    // =========================================================

    private UUID createAuditlog(CreateAuditlog cmd) {
        try {
            return auditlogPort.create(
                    cmd.actorRef(),
                    cmd.actorType(),
                    cmd.severity(),
                    cmd.originSystem(),
                    cmd.originService(),
                    cmd.originComponent(),
                    cmd.data(),
                    cmd.description()
            );
        } catch (RuntimeException ex) {
            throw new ConflictException("auditlog.not.created",
                    Map.of("object", "auditlog"));
        }
    }

    private void auditlogCheck(UUID auditId) {
        if (auditId == null || auditlogPort.findById(auditId) == null) {
            auditlogPort.compensate(auditId, getClass(), SagaOutcome.COMPENSATED);
            throw new ConflictException("auditlog.not.found.after.creation",
                    Map.of("object", "auditlog"));
        }
    }

    // =========================================================
    // USE CASES
    // =========================================================

    @Override
    public EducationResponse createEducation(CreateEducationCmd cmd) {

        UUID sessionId = cmd.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);

            UUID educationId = educationPort.createEducation(cmd);
            log.info("SAGA [{}]: education created {}", sessionId, educationId);
            
            EducationResponse education =
            educationPort.findEducationById(educationId);
            
            if (education == null || education.id() == null) {
                log.warn("SAGA [{}]: education not found after creation", sessionId);
                throw new ConflictException("education.not.found.after.creation",
                Map.of("educationId", educationId.toString()));
            }
            log.info("SAGA [{}]: education verified {}", sessionId, educationId);

            UUID auditId = createAuditlog(new CreateAuditlog(
                    cmd.sessionId(),
                    cmd.actorRef(),
                    cmd.actorType(),
                    cmd.severity(),
                    cmd.originSystem(),
                    cmd.originService(),
                    cmd.originComponent(),
                    "educationId: " + educationId,
                    cmd.description()
            ));
            log.info("SAGA [{}]: audit created {}", sessionId, auditId);

            auditlogCheck(auditId);
            log.info("SAGA [{}]: audit verified", sessionId);

            log.info("SAGA [{}]: createEducation completed", sessionId);
            return education;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationResponse updateEducationName(UUID educationId, UpdateEducationNameCmd cmd) {

        UUID sessionId = cmd.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);
            validateEducation(educationId);

            EducationResponse updated =
                    educationPort.updateEducationName(educationId, cmd.name());

            if (updated == null || updated.id() == null || !updated.active()) {
                log.warn("SAGA [{}]: education not found after update", sessionId);
                throw new ConflictException("education.not.found.after.update",
                        Map.of("educationId", educationId.toString()));
            }
                
            log.info("SAGA [{}]: education updated name {}", sessionId, educationId);

            UUID auditId = createAuditlog(new CreateAuditlog(
                    cmd.sessionId(),
                    cmd.actorRef(),
                    cmd.actorType(),
                    cmd.severity(),
                    cmd.originSystem(),
                    cmd.originService(),
                    cmd.originComponent(),
                    "educationId: " + educationId,
                    cmd.description()
            ));

            log.info("SAGA [{}]: audit created {}", sessionId, auditId);
            
            auditlogCheck(auditId);

            return updated;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationResponse updateEducationCategory(UUID educationId, UpdateEducationCategoryCmd cmd) {

        UUID sessionId = cmd.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);
            validateEducation(educationId);

            // FIXED BUG: correct method
            EducationResponse updated = educationPort.updateEducationCategory(educationId, cmd.category());

            if (updated == null || updated.id() == null || !updated.active()) {
                log.warn("SAGA [{}]: education not found after update", sessionId);
                throw new ConflictException("education.not.found.after.update",
                        Map.of("educationId", educationId.toString()));
            }

            log.info("SAGA [{}]: education updated category {}", sessionId, educationId);

            UUID auditId = createAuditlog(new CreateAuditlog(
                    cmd.sessionId(),
                    cmd.actorRef(),
                    cmd.actorType(),
                    cmd.severity(),
                    cmd.originSystem(),
                    cmd.originService(),
                    cmd.originComponent(),
                    "educationId: " + educationId,
                    cmd.description()));
                    
            log.info("SAGA [{}]: audit created {}", sessionId, auditId);
            
            auditlogCheck(auditId);

            return updated;
    
        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationResponse activateEducation(UUID educationId, CreateAuditlog auditlog) {

        UUID sessionId = auditlog.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);
            validateEducation(educationId);

            EducationResponse updated = educationPort.activateEducation(educationId);

            log.info("SAGA [{}]: education activated {}", sessionId, educationId);

            if (updated == null || updated.id() == null || !updated.active()) {
                log.warn("SAGA [{}]: education not found after activation", sessionId);
                throw new ConflictException("education.not.found.after.activation",
                        Map.of("educationId", educationId.toString()));
            }

            UUID auditId = createAuditlog(auditlog);
            log.info("SAGA [{}]: audit created {}", sessionId, auditId);
            auditlogCheck(auditId);
            return updated;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationResponse deactivateEducation(UUID educationId, CreateAuditlog auditlog) {

        UUID sessionId = auditlog.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);
            validateEducation(educationId);

            EducationResponse updated =
                    educationPort.deactivateEducation(educationId);

            log.info("SAGA [{}]: education deactivated {}", sessionId, educationId);

            if (updated == null || updated.id() == null || updated.active()) {
                log.warn("SAGA [{}]: education not found after deactivation", sessionId);
                throw new ConflictException("education.not.found.after.deactivation",
                        Map.of("educationId", educationId.toString()));
            }

            UUID auditId = createAuditlog(auditlog);
            log.info("SAGA [{}]: audit created {}", sessionId, auditId);
            auditlogCheck(auditId);
            return updated;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationInstructorResponse createEducationInstructor(CreateEducationInstructorCmd cmd) {

        UUID sessionId = cmd.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);

            EducationInstructorResponse response =
                    educationInstructorPort.createEducationInstructor(cmd);

            if (response == null || response.id() == null) {
                log.warn("SAGA [{}]: education-instructor not found after creation", sessionId);
                throw new ConflictException("educationinstructor.not.found",
                        Map.of("educationId", cmd.educationRef().toString(),
                                "instructorId", cmd.instructorRef().toString()));
            }
            
            log.info("SAGA [{}]: educationInstructor created {}", sessionId, response.id());
                    
            UUID auditId = createAuditlog(new CreateAuditlog(
                    cmd.sessionId(),
                    cmd.actorRef(),
                    cmd.actorType(),
                    cmd.severity(),
                    cmd.originSystem(),
                    cmd.originService(),
                    cmd.originComponent(),
                    "educationId: " + cmd.educationRef(),
                    cmd.description()
            ));

            log.info("SAGA [{}]: audit created {}", sessionId, auditId);

            auditlogCheck(auditId);

            return response;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationInstructorResponse activateEducationInstructor(CompositeKey id, CreateAuditlog cmd) {

        UUID sessionId = cmd.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);
            validateEducationInstructor(id);

            EducationInstructorResponse response =
                    educationInstructorPort.activateEducationInstructor(id);
            
            log.info("SAGA [{}]: educationInstructor activated {}", sessionId, id);

            if (response == null || response.id() == null || !response.isActive()) {
                log.warn("SAGA [{}]: education-instructor not found after activation", sessionId);
                throw new ConflictException("educationinstructor.not.found.after.activation",
                        Map.of("educationInstructorId", id.toString()));
            }

            UUID auditId = createAuditlog(cmd);
            log.info("SAGA [{}]: audit created {}", sessionId, auditId);
            auditlogCheck(auditId);
            return response;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }

    @Override
    public EducationInstructorResponse deactivateEducationInstructor(CompositeKey id, CreateAuditlog cmd) {

        UUID sessionId = cmd.sessionId();
        acquireLock(sessionId);

        try {
            log.info("SAGA [{}]: lock acquired", sessionId);
            validateEducationInstructor(id);

            EducationInstructorResponse response =
                    educationInstructorPort.deactivateEducationInstructor(id);

            log.info("SAGA [{}]: educationInstructor deactivated {}", sessionId, id);

            if (response == null || response.id() == null || response.isActive()) {
                log.warn("SAGA [{}]: education-instructor not found after deactivation", sessionId);
                throw new ConflictException("educationinstructor.not.found.after.deactivation",
                        Map.of("educationInstructorId", id.toString()));
            }

            UUID auditId = createAuditlog(cmd);
            log.info("SAGA [{}]: audit created {}", sessionId, auditId);
            auditlogCheck(auditId);
            return response;

        } catch (ConflictException ex) {
            log.error("SAGA [{}]: conflict error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: runtime error - {}", sessionId, ex.getMessage());
            compensate(sessionId);
            throw ex;
        } finally {
            releaseLock(sessionId);
        }
    }
}