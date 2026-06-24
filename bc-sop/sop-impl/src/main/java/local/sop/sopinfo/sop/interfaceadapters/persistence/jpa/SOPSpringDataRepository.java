package local.sop.sopinfo.sop.interfaceadapters.persistence.jpa;



import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SOPSpringDataRepository extends JpaRepository<SOPEntity, UUID> {

}
