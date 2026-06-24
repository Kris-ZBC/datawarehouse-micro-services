package local.sop.sopinfo.apprentice.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.apprentice.domain.model.Apprentice;
import local.sop.sopinfo.apprentice.domain.model.valueobjects.ApprenticeId;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ApprenticeRepositoryPort {
    Apprentice save(Apprentice apprentice);

    Optional<Apprentice> findById(UUID id);

    List<Apprentice> findByEducationLineId(UUID educationLineId);

    List<Apprentice> findAll();
    
    Boolean compensate(ApprenticeId id, SagaOutcome sagaState);
}
