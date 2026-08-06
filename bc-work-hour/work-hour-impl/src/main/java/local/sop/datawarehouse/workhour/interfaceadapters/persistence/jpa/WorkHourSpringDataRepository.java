package local.sop.datawarehouse.workhour.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkHourSpringDataRepository extends JpaRepository<WorkHourEntity, UUID> {

}