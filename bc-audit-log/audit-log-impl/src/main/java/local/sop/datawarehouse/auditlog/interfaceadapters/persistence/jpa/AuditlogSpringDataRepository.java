package local.sop.datawarehouse.auditlog.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;


public interface AuditlogSpringDataRepository extends JpaRepository<AuditlogEntity, UUID> {
    @Query("SELECT a FROM AuditlogEntity a WHERE "
         + "(:id IS NULL OR a.id = :id) AND "
         + "(:actorRef IS NULL OR a.actorRef = :actorRef) AND "
         + "(:actorType IS NULL OR a.actorType = :actorType) AND "
         + "(:originSystem IS NULL OR a.originSystem = :originSystem) AND "
         + "(:originService IS NULL OR a.originService = :originService) AND "
         + "(:originComponent IS NULL OR a.originComponent = :originComponent) AND "
         + "(:severity IS NULL OR a.severity = :severity)")
    List<AuditlogEntity> findBySearchParams(
            @Param("id") UUID id,
            @Param("actorRef") UUID actorRef,
            @Param("actorType") ActorType actorType,
            @Param("originSystem") String originSystem,
            @Param("originService") String originService,
            @Param("originComponent") String originComponent,
            @Param("severity") Severity severity);
     
     @Modifying(clearAutomatically = true)
	@Query("DELETE AuditlogEntity a WHERE a.id = id")
     int delete(@Param("id") UUID id);
}
