package local.sop.sopinfo.sopeducation.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopEducationSpringDataRepository extends JpaRepository<SopEducationEntity, SopEducationId> {

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE SopEducationEntity s 
        SET s.active = :active
        WHERE s.id = :id
        """)
    int updateActiveById(
        @Param("id") SopEducationId id,
        @Param("active") boolean active
    );

    @Query("SELECT s FROM SopEducationEntity s WHERE s.id.sopRef = :sopRef")
    List<SopEducationEntity> findBySopRef(@Param("sopRef") UUID sopRef);

    @Query("SELECT s FROM SopEducationEntity s WHERE s.id.educationRef = :educationRef")
    List<SopEducationEntity> findByEducationRef(@Param("educationRef") UUID educationRef);
}
