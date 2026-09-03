package local.sop.datawarehouse.apprentice.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApprenticeSpringDataRepository extends JpaRepository<ApprenticeEntity, UUID> {
    List<ApprenticeEntity> findByEducationLineRef(UUID educationLineRef);

    // NEW
    Optional<ApprenticeEntity> findByPersonRef(UUID personRef);

    @Modifying(clearAutomatically = true)
	@Query("DELETE FROM ApprenticeEntity a WHERE a.id = :id")
	int delete(@Param("id") UUID id);
}

