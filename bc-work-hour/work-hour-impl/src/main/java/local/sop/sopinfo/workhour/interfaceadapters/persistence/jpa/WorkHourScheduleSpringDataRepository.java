package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;

import java.util.List;
import java.util.Optional;


public interface WorkHourScheduleSpringDataRepository extends JpaRepository<WorkHourScheduleEntity, UUID> {
    Optional<WorkHourScheduleEntity> findById(UUID id);

    @Query("SELECT s from WorkHourScheduleEntity s WHERE "+
        "(:id IS NULL OR s.id = :id) AND " +
        "(:startTime IS NULL OR s.startTime = :startTime) AND "+
        "(:endTime IS NULL OR s.endTime = :endTime) AND "+
        "(:weekDay IS NULL OR s.weekDay = :weekDay) AND "+
        "(:sopRef IS NULL OR s.sopRef = :sopRef) ")
    List<WorkHourScheduleEntity> findBySearchParams(
        @Param("id") UUID id,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("weekDay") WeekDay weekDay,
        @Param("sopRef") UUID sopRef);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE WorkHourScheduleEntity s 
        SET s.startTime = COALESCE(:startTime, s.startTime),
            s.endTime = COALESCE(:endTime, s.endTime),
            s.weekDay = COALESCE(:weekDay, s.weekDay),
            s.sopRef = COALESCE(:sopRef, s.sopRef)
        WHERE s.id = :id
        """)
    int updateWorkHourSchedule(
        @Param("id") UUID id,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("weekDay") WeekDay weekDay,
        @Param("sopRef") UUID sopRef
    );   
    
    

    
}
