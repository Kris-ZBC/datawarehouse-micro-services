package local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

@Component
public final class PersonNotificationJpaMapper {

    public PersonNotification toDomain(PersonNotificationEntity e) {
        return PersonNotification.builder()
                .id(new CompositeKey(e.getId().getPersonRef(), e.getId().getNotificationRef()))
                .active(e.isActive())
                .createdAt(new CreatedAtTimestamp(e.getCreatedAt()))
                .build();
    }

    public PersonNotificationEntity toEntity(PersonNotification pn) {
        return PersonNotificationEntity.builder()
                .id(new PersonNotificationId(pn.getId().key1(), pn.getId().key2()))
                .active(pn.isActive())
                .build();
    }

    public void updateIntoEntity(PersonNotification pn, PersonNotificationEntity entity) {
        entity.withActive(pn.isActive());
    }

}
