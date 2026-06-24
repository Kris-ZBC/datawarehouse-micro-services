package local.sop.sopinfo.educationline.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EducationLineSpringDataRepository extends JpaRepository<EducationLineEntity, UUID> {
	@Query("SELECT e FROM EducationLineEntity e WHERE "
		+ "(:id IS NULL OR e.id = :id) AND "
		+ "(:name IS NULL OR e.name LIKE %:name%)")
	List<EducationLineEntity> findBySearchParams(
		@Param("id") UUID id,
		@Param("name") String name
	);

	// COALESCE is used to only update the fields that are provided (non-null)
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationLineEntity e SET "
			+ "e.name = COALESCE(:name, e.name), "
			+ "e.durationYears = COALESCE(:durationYears, e.durationYears), "
			+ "e.durationMonths = COALESCE(:durationMonths, e.durationMonths), "
			+ "e.durationDays = COALESCE(:durationDays, e.durationDays) "
			+ "WHERE e.id = :id")
	void update(@Param("id") UUID id,
				@Param("name") String name,
				@Param("durationYears") Integer durationYears,
				@Param("durationMonths") Integer durationMonths,
				@Param("durationDays") Integer durationDays);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationLineEntity e SET e.active = false WHERE e.id = :id")
	void deactivate(@Param("id") UUID id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE EducationLineEntity e SET e.active = true WHERE e.id = :id")
	void activate(@Param("id") UUID id);

	@Query("SELECT e FROM EducationLineEntity e WHERE e.educationRef = :educationRef")
	List<EducationLineEntity> findByEducationRef(@Param("educationRef") UUID id);


	@Query("DELETE FROM EducationLineEntity e WHERE e.id = :id")
	int delete(@Param("id") UUID id);
	@Query("UPDATE EducationLineEntity e SET e.active = true WHERE e.id = :id")
	boolean compensateActivate(@Param("id") UUID id);
	@Query("UPDATE EducationLineEntity e SET e.active = false WHERE e.id = :id")
	boolean compensateDeactivate(@Param("id") UUID id);
	@Query("UPDATE EducationLineEntity e SET e.name = :name WHERE e.id = :id")
	boolean compensateName(@Param("id") UUID id, @Param("name") String name);
	@Query("UPDATE EducationLineEntity e SET e.durationYears = :durationYears, e.durationMonths = :durationMonths, e.durationDays = :durationDays WHERE e.id = :id")
	boolean compensateDuration(@Param("id") UUID id, @Param("durationYears") Integer durationYears, @Param("durationMonths") Integer durationMonths, @Param("durationDays") Integer durationDays);
}
