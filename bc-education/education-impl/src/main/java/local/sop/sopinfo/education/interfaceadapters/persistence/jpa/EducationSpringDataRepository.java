package local.sop.sopinfo.education.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EducationSpringDataRepository extends JpaRepository<EducationEntity, UUID> {
    
    boolean existsByName(String name);
    
    boolean existsByCategory(String category);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE EducationEntity e SET e.active = true WHERE e.id = :id")
    void activate(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE EducationEntity e SET e.active = false WHERE e.id = :id")
    void deactivate(@Param("id") UUID id);

    @Query("DELETE FROM EducationEntity e WHERE e.id = :id")
    void deleteById(@Param("id") UUID id);

    @Query("UPDATE EducationEntity e SET e.name = :name WHERE e.id = :id")
    void updateName(@Param("id") UUID id, @Param("name") String name);

    @Query("UPDATE EducationEntity e SET e.category = :category WHERE e.id = :id")
    void updateCategory(@Param("id") UUID id, @Param("category") String category);

    @Query("UPDATE EducationEntity e SET e.active = :active WHERE e.id = :id")
    void updateActive(@Param("id") UUID id, @Param("active") Boolean active);
}