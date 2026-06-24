package local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.ports.out.SopInstructorPort;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;

/* preconditions
Key1 and Key2 of Compositekey must be non-null and valid UUIDs strings that correspond to sopRef and instructorRef respectively in the SopInstructorEntity
*/

@Repository
public class SopInstructorRepositoryAdapter implements SopInstructorPort {
    private final SopInstructorSpringDataRepository repo;
    private final SopInstructorDomainJpaMapper mapper;

    public SopInstructorRepositoryAdapter(SopInstructorSpringDataRepository repo, SopInstructorDomainJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<SopInstructor> findById(CompositeKey id) {
        SopInstructorId sopInstructorId = new SopInstructorId(id.key1(), id.key2());
        return Optional.ofNullable(repo.findById(sopInstructorId)
            .map(mapper::toDomain)
            .orElse(null));
    }

    @Override
    public SopInstructor save(SopInstructor s) {
        SopInstructorEntity entity = mapper.toEntity(s);
        entity = repo.save(entity);
        entity = repo.findById(entity.getId()).orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "id", s.getId())));
        return mapper.toDomain(entity);
    }

    @Override
    public void update(SopInstructor s) {
        SopInstructor existing = findById(s.getId())
            .orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "id", s.getId().toString())));
        SopInstructorEntity entity = mapper.toEntity(existing);
        mapper.updateIntoEntity(s, entity);
        repo.updateActiveById(entity.getId(), s.isActive());
    }

    @Override
    public List<SopInstructor> findBySopRef(UUID sopRef) {
        List<SopInstructorEntity> entities = repo.findBySopRef(sopRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SopInstructor> findByInstructorRef(UUID instructorRef) {
        List<SopInstructorEntity> entities = repo.findByInstructorRef(instructorRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

}
