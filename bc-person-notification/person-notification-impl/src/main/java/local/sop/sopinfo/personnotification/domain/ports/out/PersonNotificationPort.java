package local.sop.sopinfo.personnotification.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public interface PersonNotificationPort {
    Optional<PersonNotification> findById(CompositeKey id);
    PersonNotification save(PersonNotification PersonNotification);
    void update(PersonNotification PersonNotification);
    List<PersonNotification> findByPersonRef(UUID personRef);
    List<PersonNotification> findByNotificationRef(UUID notificationRef);
}
