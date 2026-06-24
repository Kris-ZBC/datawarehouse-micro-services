package local.sop.sopinfo.organisation.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationSpringDataRepository extends JpaRepository<OrganisationEntity, UUID> {
}
