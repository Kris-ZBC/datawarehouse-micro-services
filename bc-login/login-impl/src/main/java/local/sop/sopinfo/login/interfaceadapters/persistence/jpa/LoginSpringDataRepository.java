package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;


public interface LoginSpringDataRepository extends JpaRepository<LoginEntity, UUID> {
	Optional<LoginEntity> findByUsername(String username);
	@Override @NonNull Optional<LoginEntity> findById(@NonNull UUID id);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM LoginEntity l WHERE l.id = :id")
    int delete(@Param("id") UUID id);
}
