package local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SopInstructorSpringDataRepository extends JpaRepository<SopInstructorEntity, SopInstructorId> {

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE SopInstructorEntity me
        SET me.active = :active
        WHERE me.id = :id
    """)
    int updateActiveById(
        @Param("id") SopInstructorId id,
        @Param("active") Boolean active
    );

    @Query("SELECT s FROM SopInstructorEntity s WHERE s.id.sopRef = :sopRef")
    List<SopInstructorEntity> findBySopRef(@Param("sopRef") UUID sopRef);

    @Query("SELECT s FROM SopInstructorEntity s WHERE s.id.instructorRef = :instructorRef")
    List<SopInstructorEntity> findByInstructorRef(@Param("instructorRef") UUID instructorRef);
}
