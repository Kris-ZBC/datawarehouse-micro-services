package local.sop.sopinfo.message.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageSagaStateSpringDataRepository extends JpaRepository<MessageSagaStateEntity, UUID> {

}
