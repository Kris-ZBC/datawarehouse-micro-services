package local.sop.sopinfo.consent.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentSagaStateSpringDataRepository extends JpaRepository<ConsentSagaStateEntity, UUID> {

}
