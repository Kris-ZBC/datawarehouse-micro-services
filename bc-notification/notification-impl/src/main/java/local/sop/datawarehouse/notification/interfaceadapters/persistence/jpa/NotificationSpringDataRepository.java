package local.sop.datawarehouse.notification.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationSpringDataRepository extends JpaRepository<NotificationEntity, UUID> {

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM NotificationEntity n WHERE n.id = :id")
	int delete(@Param("id") UUID id);
}
