package local.sop.datawarehouse.sopeducation.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.datawarehouse.sopeducation.domain.model.SopEducation;
import local.sop.datawarehouse.sopeducation.domain.ports.out.SopEducationPort;

/* preconditions 
 * Key1 and Key2 of CompositeKey must be non-null and valid UUID strings that correspond to sopRef and educationLineRef in the SopEducationLineEntity   
*/

@Repository
public class SopEducationRepositoryAdapter implements SopEducationPort {
    private final SopEducationSpringDataRepository repo;
    private final SopEducationDomainJpaMapper mapper;

    public SopEducationRepositoryAdapter(SopEducationSpringDataRepository repo, SopEducationDomainJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<SopEducation> findById(CompositeKey id) {
        SopEducationId sopEducationLineId = new SopEducationId(id.key1(), id.key2());
        return Optional.ofNullable(repo.findById(sopEducationLineId)
            .map(mapper::toDomain)
            .orElse(null));
    }

    @Override
    public SopEducation save(SopEducation s) {
        SopEducationEntity entity = mapper.toEntity(s);
        entity = repo.save(entity);
        entity = repo.findById(entity.getId()).orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "value", s.getId().toString())));
        return mapper.toDomain(entity);
    }

    @Override
    public void update(SopEducation s) {
        SopEducation existing = findById(s.getId())
            .orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "value", s.getId().toString())));
        SopEducationEntity entity = mapper.toEntity(existing);
        mapper.updateEntity(s, entity);
        repo.updateActiveById(entity.getId(), s.isActive());
    }

    @Override
    public List<SopEducation> findBySopRef(UUID sopRef) {
        List<SopEducationEntity> entities = repo.findBySopRef(sopRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SopEducation> findByEducationRef(UUID educationId) {
        List<SopEducationEntity> entities = repo.findByEducationRef(educationId);
        return entities.stream().map(mapper::toDomain).toList();
    }
    
}
