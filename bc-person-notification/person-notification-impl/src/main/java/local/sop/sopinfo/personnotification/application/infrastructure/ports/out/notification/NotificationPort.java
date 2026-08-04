package local.sop.sopinfo.personnotification.application.infrastructure.ports.out.notification;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.personnotification.application.infrastructure.response.NotificationResponse;
import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface NotificationPort extends CompositeKeyValidator {
        Optional<NotificationResponse> findById(UUID id);
}
