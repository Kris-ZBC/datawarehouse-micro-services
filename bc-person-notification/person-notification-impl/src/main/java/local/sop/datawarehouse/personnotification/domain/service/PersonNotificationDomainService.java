package local.sop.datawarehouse.personnotification.domain.service;


import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.personnotification.domain.model.PersonNotification;
import local.sop.datawarehouse.personnotification.domain.model.valueobjects.CreatedAtTimestamp;

public class PersonNotificationDomainService implements PersonNotificationDomain {

    @Override
    public PersonNotification createPersonNotification(CompositeKey id, Boolean active) {
        return new PersonNotification.Builder()
                .id(id)
                .active(active)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();
    }

    @Override
    public PersonNotification toggleActivatePersonNotification(CompositeKey id, Boolean previousActive, LocalDateTime createdAt) {
        return new PersonNotification.Builder()
                .id(id)
                .active(!previousActive)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();
    }


    @Override
    public PersonNotification deletePersonNotification(CompositeKey id) {
        
        return new PersonNotification.Builder()
                .id(id)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();
    }

}
