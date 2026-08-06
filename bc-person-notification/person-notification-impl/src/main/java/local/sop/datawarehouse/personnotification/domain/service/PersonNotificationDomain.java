package local.sop.datawarehouse.personnotification.domain.service;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.personnotification.domain.model.PersonNotification;


public interface PersonNotificationDomain {
    public PersonNotification createPersonNotification(CompositeKey id, Boolean active);
    public PersonNotification toggleActivatePersonNotification(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public PersonNotification deletePersonNotification(CompositeKey id);
}