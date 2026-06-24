package local.sop.sopinfo.person.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonSpringDataRepository extends JpaRepository<PersonEntity, UUID> {
	Optional<PersonEntity> findByEmail(String email);
	List<PersonEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
		String firstName,
		String lastName
	);
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM PersonEntity p WHERE p.id = :id")
	int delete(@Param("id") UUID id);
}