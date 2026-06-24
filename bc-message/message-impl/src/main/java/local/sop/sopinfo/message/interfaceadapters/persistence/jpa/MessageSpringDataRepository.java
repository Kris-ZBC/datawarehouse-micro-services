package local.sop.sopinfo.message.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageSpringDataRepository extends JpaRepository<MessageEntity, UUID> {

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM MessageEntity m WHERE m.id = :id")
	int delete(@Param("id") UUID id);
}