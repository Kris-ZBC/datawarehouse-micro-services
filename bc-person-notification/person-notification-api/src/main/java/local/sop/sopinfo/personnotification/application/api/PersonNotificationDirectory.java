package local.sop.sopinfo.personnotification.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.personnotification.application.api.dto.CreatePersonNotificationCmd;
import local.sop.sopinfo.personnotification.application.api.dto.CreatedPersonNotificationResult;
import local.sop.sopinfo.personnotification.application.api.dto.PersonNotificationResponse;
import local.sop.sopinfo.personnotification.application.api.dto.ToggleActivatePersonNotificationCmd;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public interface PersonNotificationDirectory {
    Optional<PersonNotificationResponse> findById(CompositeKey id);
    CreatedPersonNotificationResult create(CreatePersonNotificationCmd cmd);
    PersonNotificationResponse toggleActive(ToggleActivatePersonNotificationCmd cmd);
    List<PersonNotificationResponse> getByPersonRef(UUID personRef);
    List<PersonNotificationResponse> getByNotificationRef(UUID notificationRef);
}
