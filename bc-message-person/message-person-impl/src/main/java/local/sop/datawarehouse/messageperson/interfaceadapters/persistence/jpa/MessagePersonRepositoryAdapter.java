package local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;
import local.sop.datawarehouse.messageperson.domain.ports.out.MessagePersonPort;

/* preconditions
Key1 and Key2 of Compositekey must be non-null and valid UUIDs strings that correspond to messageRef and personRef respectively in the MessagePersonEntity
*/

@Repository
public class MessagePersonRepositoryAdapter implements MessagePersonPort {
    private final MessagePersonSpringDataRepository repo;
    private final MessagePersonDomainJpaMapper mapper;

    public MessagePersonRepositoryAdapter(MessagePersonSpringDataRepository repo, MessagePersonDomainJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<MessagePerson> findById(CompositeKey id) {
        MessagePersonId messagePersonId = new MessagePersonId(id.key1(), id.key2());
        return Optional.ofNullable(repo.findById(messagePersonId)
            .map(mapper::toDomain)
            .orElse(null));
    }

    @Override
    public MessagePerson save(MessagePerson s) {
        MessagePersonEntity entity = mapper.toEntity(s);
        entity = repo.save(entity);
        entity = repo.findById(entity.getId()).orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "id", s.getId())));
        return mapper.toDomain(entity);
    }

    @Override
    public void update(MessagePerson s) {
        MessagePerson existing = findById(s.getId())
            .orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "id", s.getId().toString())));
        MessagePersonEntity entity = mapper.toEntity(existing);
        mapper.updateIntoEntity(s, entity);
        repo.updateActiveById(entity.getId(), s.isActive());
    }

    @Override
    public List<MessagePerson> findByMessageRef(UUID messageRef) {
        List<MessagePersonEntity> entities = repo.findByMessageRef(messageRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<MessagePerson> findByPersonRef(UUID personRef) {
        List<MessagePersonEntity> entities = repo.findByPersonRef(personRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

}
