package local.sop.datawarehouse.education.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationSagaStateSpringDataRepository extends JpaRepository<EducationSagaStateEntity, UUID> {}
