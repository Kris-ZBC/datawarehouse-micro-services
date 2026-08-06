package local.sop.datawarehouse.sopinstructor.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.sopinstructor.domain.model.SopInstructor;

public interface SopInstructorPort {
    Optional<SopInstructor> findById(CompositeKey id);
    SopInstructor save(SopInstructor sopInstructor);
    void update(SopInstructor sopInstructor);
    List<SopInstructor> findBySopRef(UUID sopRef);
    List<SopInstructor> findByInstructorRef(UUID instructorRef);
}
