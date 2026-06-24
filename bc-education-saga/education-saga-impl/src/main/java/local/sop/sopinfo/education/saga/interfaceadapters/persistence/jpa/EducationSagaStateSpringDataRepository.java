package local.sop.sopinfo.education.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationSagaStateSpringDataRepository extends JpaRepository<EducationSagaStateEntity, UUID> {}
