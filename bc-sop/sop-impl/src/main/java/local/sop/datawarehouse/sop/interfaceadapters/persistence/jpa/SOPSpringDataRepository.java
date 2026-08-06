package local.sop.datawarehouse.sop.interfaceadapters.persistence.jpa;



import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SOPSpringDataRepository extends JpaRepository<SOPEntity, UUID> {

}
