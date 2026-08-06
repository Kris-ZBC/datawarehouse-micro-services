package local.sop.datawarehouse.personnotification.application.infrastructure.ports.out.notification;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.datawarehouse.personnotification.application.infrastructure.response.NotificationResponse;

public interface NotificationPort extends CompositeKeyValidator {
        Optional<NotificationResponse> findById(UUID id);
}
