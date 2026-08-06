package local.sop.datawarehouse.organisation.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationSpringDataRepository extends JpaRepository<OrganisationEntity, UUID> {
}
