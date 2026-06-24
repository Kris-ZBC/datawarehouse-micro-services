package local.sop.sopinfo.notification.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSpringDataRepository extends JpaRepository<NotificationEntity, UUID> {
}
