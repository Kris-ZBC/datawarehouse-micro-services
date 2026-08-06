package local.sop.datawarehouse.educationinstructor.interfaceadapters.persistence.jpa;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EISpringDataRepository extends JpaRepository<EIEntity, EIId> {

	@Modifying(clearAutomatically = true)
	@Query("""
		UPDATE EducationInstructorEntity e
		SET e.active = :active
		WHERE e.id = :id
	""")
	int updateActiveById(
		@Param("id") EIId id,
		@Param("active") Boolean active
	);

	@Query("SELECT e FROM EducationInstructorEntity e WHERE e.id.educationRef = :educationRef AND e.id.instructorRef = :instructorRef")
	public Optional<EIEntity> findByEducationRefAndInstructorRef(@Param("educationRef") UUID educationRef, @Param("instructorRef") UUID instructorRef);

	@Query("SELECT e FROM EducationInstructorEntity e WHERE e.id.educationRef = :educationRef")
	public List<EIEntity> findByEducationRef(@Param("educationRef") UUID educationRef);

	@Query("SELECT e FROM EducationInstructorEntity e WHERE e.id.instructorRef = :instructorRef")
	public List<EIEntity> findByInstructorRef(@Param("instructorRef") UUID instructorRef);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationInstructorEntity e SET e.active = false WHERE e.id.educationRef = :educationRef AND e.id.instructorRef = :instructorRef")
	public void deactivateByEducationRefAndInstructorRef(@Param("educationRef") UUID educationRef, @Param("instructorRef") UUID instructorRef);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationInstructorEntity e SET e.active = true WHERE e.id.educationRef = :educationRef AND e.id.instructorRef = :instructorRef")
	public void activateByEducationRefAndInstructorRef(@Param("educationRef") UUID educationRef, @Param("instructorRef") UUID instructorRef);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM EducationInstructorEntity e WHERE e.id.educationRef = :educationRef AND e.id.instructorRef = :instructorRef")
	public void delete(@Param("educationRef") UUID educationRef, @Param("instructorRef") UUID instructorRef);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationInstructorEntity e SET e.active = true WHERE e.id.educationRef = :educationRef AND e.id.instructorRef = :instructorRef")
	public void compensateDeactivateByEducationRefAndInstructorRef(@Param("educationRef") UUID educationRef, @Param("instructorRef") UUID instructorRef);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationInstructorEntity e SET e.active = false WHERE e.id.educationRef = :educationRef AND e.id.instructorRef = :instructorRef")
	public void compensateActivateByEducationRefAndInstructorRef(@Param("educationRef") UUID educationRef, @Param("instructorRef") UUID instructorRef);
}
