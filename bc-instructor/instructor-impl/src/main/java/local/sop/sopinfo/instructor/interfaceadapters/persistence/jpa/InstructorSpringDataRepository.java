package local.sop.sopinfo.instructor.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstructorSpringDataRepository extends JpaRepository<InstructorEntity, UUID> {
    Optional<InstructorEntity> findByEmail(String email);
    List<InstructorEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName,
        String lastName
    );

    @Modifying(clearAutomatically = true)
	@Query("DELETE FROM InstructorEntity i WHERE i.id = :id")
	int delete(@Param("id") UUID id);
}