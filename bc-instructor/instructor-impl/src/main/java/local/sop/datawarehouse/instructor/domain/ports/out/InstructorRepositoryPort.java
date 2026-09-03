package local.sop.datawarehouse.instructor.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.instructor.domain.model.Instructor;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.InstructorId;

public interface InstructorRepositoryPort {

    Instructor save(Instructor instructor);

    Boolean exists();

    List<Instructor> findAll();

    Optional<Instructor> findById(InstructorId id);

    Boolean compensate(InstructorId id, SagaOutcome sagaState);
}