package local.sop.datawarehouse.apprentice.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.apprentice.domain.model.Apprentice;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.ApprenticeId;

public interface ApprenticeRepositoryPort {
    Apprentice save(Apprentice apprentice);

    Optional<Apprentice> findById(UUID id);

    List<Apprentice> findByEducationLineId(UUID educationLineId);

    List<Apprentice> findAll();

        // NEW: role resolution for login-saga.
    Optional<Apprentice> findByPersonRef(UUID personRef);
    
    Boolean compensate(ApprenticeId id, SagaOutcome sagaState);
}
