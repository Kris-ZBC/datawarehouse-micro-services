package local.sop.datawarehouse.personnotification.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonNotificationSpringDataRepository extends JpaRepository<PersonNotificationEntity, PersonNotificationId> {

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE PersonNotificationEntity pn
        SET pn.active = :active
        WHERE pn.id = :id
    """)
    int updateActiveById(
        @Param("id") PersonNotificationId id,
        @Param("active") Boolean active
    );

    @Query("SELECT pn FROM PersonNotificationEntity pn WHERE pn.id.personRef = :personRef")
    List<PersonNotificationEntity> findByPersonRef(@Param("personRef") UUID personRef);

    @Query("SELECT pn FROM PersonNotificationEntity pn WHERE pn.id.notificationRef = :notificationRef")
    List<PersonNotificationEntity> findByNotificationRef(@Param("notificationRef") UUID notificationRef);

}

