package local.sop.datawarehouse.instructor.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.instructor.domain.model.Instructor;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.InstructorId;

public interface InstructorRepositoryPort {

    Instructor save(Instructor instructor);

    Boolean exists();

    List<Instructor> findAll();

    Optional<Instructor> findById(InstructorId id);

       // NEW: needed by login-saga to resolve role at session-creation
    // time — "does this person have an Instructor record at all".
    Optional<Instructor> findByPersonRef(UUID personRef);

    Boolean compensate(InstructorId id, SagaOutcome sagaState);
}