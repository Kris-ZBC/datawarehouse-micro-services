package local.sop.sopinfo.personnotification.domain.service;

import java.time.LocalDateTime;
import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;


public interface PersonNotificationDomain {
    public PersonNotification createPersonNotification(CompositeKey id, Boolean active);
    public PersonNotification toggleActivatePersonNotification(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public PersonNotification deletePersonNotification(CompositeKey id);
}