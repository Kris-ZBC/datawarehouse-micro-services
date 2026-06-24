package local.sop.sopinfo.messageperson.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessagePersonSpringDataRepository extends JpaRepository<MessagePersonEntity, MessagePersonId> {

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE MessagePersonEntity me
        SET me.active = :active
        WHERE me.id = :id
    """)
    int updateActiveById(
        @Param("id") MessagePersonId id,
        @Param("active") Boolean active
    );

    @Query("SELECT s FROM MessagePersonEntity s WHERE s.id.messageRef = :messageRef")
    List<MessagePersonEntity> findByMessageRef(@Param("messageRef") UUID messageRef);

    @Query("SELECT s FROM MessagePersonEntity s WHERE s.id.personRef = :personRef")
    List<MessagePersonEntity> findByPersonRef(@Param("personRef") UUID personRef);
}
