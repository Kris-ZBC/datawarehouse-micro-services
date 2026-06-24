package local.sop.sopinfo.anonymize.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface AnonymizeRepositoryPort {

    Anonymize save(Anonymize anonymizeLog);

    Optional<Anonymize> findById(AnonymizeId anonymizationId);

    List<Anonymize> findBySearchParams(AnonymizeId anonymizationId, PersonRef personRef);

    List<Anonymize> findAll();

    Boolean compensate(AnonymizeId anonymizationId, SagaOutcome sagaState);
}
