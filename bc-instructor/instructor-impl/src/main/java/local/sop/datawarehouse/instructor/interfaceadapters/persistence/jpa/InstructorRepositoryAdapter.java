package local.sop.datawarehouse.instructor.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.instructor.domain.model.Instructor;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.InstructorId;
import local.sop.datawarehouse.instructor.domain.ports.out.InstructorRepositoryPort;


@Repository
@Qualifier("JpaInstructorRepository")
public class InstructorRepositoryAdapter implements InstructorRepositoryPort {

    private final InstructorSpringDataRepository jpaRepository;

    public InstructorRepositoryAdapter(InstructorSpringDataRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Instructor save(Instructor instructor) {
        InstructorEntity entity = InstructorJpaMapper.toEntity(instructor);
        InstructorEntity savedEntity = jpaRepository.save(entity);
        return InstructorJpaMapper.toDomain(savedEntity);
    }

    @Override
    public List<Instructor> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(InstructorJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Instructor> findById(InstructorId id) {
        return jpaRepository.findById(id.value())
                .map(InstructorJpaMapper::toDomain);
    }

    @Override
    public Boolean compensate(InstructorId id, SagaOutcome sagaState) {
        if(sagaState != SagaOutcome.COMPENSATE)
            throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));

        var found = findById(id);
        if(found.isEmpty()) {
            return false;
        }
        return (jpaRepository.delete(id.value()) == 1);
    }

    @Override
    public Boolean exists() {
        return jpaRepository.count() > 0;
    }
}