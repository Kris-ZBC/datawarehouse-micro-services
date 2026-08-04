package local.sop.sopinfo.instructor.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.instructor.domain.model.Instructor;
import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface InstructorRepositoryPort {

    Instructor save(Instructor instructor);

    List<Instructor> findAll();

    Optional<Instructor> findById(InstructorId id);

    Boolean compensate(InstructorId id, SagaOutcome sagaState);
}