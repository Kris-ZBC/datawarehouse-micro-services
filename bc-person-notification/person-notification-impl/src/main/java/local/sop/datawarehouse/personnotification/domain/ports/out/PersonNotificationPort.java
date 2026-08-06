package local.sop.datawarehouse.personnotification.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.personnotification.domain.model.PersonNotification;

public interface PersonNotificationPort {
    Optional<PersonNotification> findById(CompositeKey id);
    PersonNotification save(PersonNotification PersonNotification);
    void update(PersonNotification PersonNotification);
    List<PersonNotification> findByPersonRef(UUID personRef);
    List<PersonNotification> findByNotificationRef(UUID notificationRef);
}
