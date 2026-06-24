package local.sop.sopinfo.institution.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionSpringDataRepository extends JpaRepository<InstitutionEntity, UUID> {
}
