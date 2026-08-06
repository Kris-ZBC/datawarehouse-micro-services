package local.sop.datawarehouse.anonymize.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AnonymizeSpringDataRepository extends JpaRepository<AnonymizeEntity, UUID> {

    Optional<AnonymizeEntity> findById(UUID id);

    Optional<AnonymizeEntity> findByIdAndPersonRef(UUID id, UUID personRef);

    List<AnonymizeEntity> findByIdOrPersonRef(UUID id, UUID personRef);
    @Query("SELECT a FROM AnonymizeEntity a WHERE (:id IS NULL OR a.id = :id) AND (:personRef IS NULL OR a.personRef = :personRef)")

    List<AnonymizeEntity> findByPersonRef(UUID personRef);

    @Query("SELECT a FROM AnonymizeEntity a")
    List <AnonymizeEntity> findAll();

    @Modifying
    @Transactional
    @Query("DELETE FROM AnonymizeEntity a WHERE a.id = :id")
    int delete(UUID id);
}
