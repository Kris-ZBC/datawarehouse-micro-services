package local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.domain.ports.out.PersonNotificationPort;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;

@Repository
public class PersonNotificationRepositoryAdapter implements PersonNotificationPort {

    private final PersonNotificationSpringDataRepository repo;
    private final PersonNotificationJpaMapper mapper;

    public PersonNotificationRepositoryAdapter(PersonNotificationSpringDataRepository repo, PersonNotificationJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<PersonNotification> findById(CompositeKey id) {
        PersonNotificationId personNotificationId = new PersonNotificationId(id.key1(), id.key2());
        return Optional.ofNullable(repo.findById(personNotificationId)
            .map(mapper::toDomain)
            .orElse(null));
    }

    @Override
    public PersonNotification save(PersonNotification relation) {
        PersonNotificationEntity entity = mapper.toEntity(relation);
        entity = repo.save(entity);
        entity = repo.findById(entity.getId()).orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "id", relation.getId())));
        return mapper.toDomain(entity);
    }

    @Override
    public void update(PersonNotification relation) {
        PersonNotification existing = findById(relation.getId())
            .orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "id", relation.getId().toString())));
        PersonNotificationEntity entity = mapper.toEntity(existing);
        mapper.updateIntoEntity(relation, entity);
        repo.updateActiveById(entity.getId(), relation.isActive());
    }

    /*
    @Override
    public void update(PersonNotification p) {
        PersonNotification existing = findById(p.getId())
            .orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "value", p.getId().toString())));
        PersonNotificationEntity entity = mapper.toEntity(existing);
        mapper.updateEntity(p, entity);
    }
    */

    @Override
    public List<PersonNotification> findByPersonRef(UUID personRef) {
        List<PersonNotificationEntity> entities = repo.findByPersonRef(personRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PersonNotification> findByNotificationRef(UUID notificationRef) {
        List<PersonNotificationEntity> entities = repo.findByNotificationRef(notificationRef);
        return entities.stream().map(mapper::toDomain).toList();
    }

}
