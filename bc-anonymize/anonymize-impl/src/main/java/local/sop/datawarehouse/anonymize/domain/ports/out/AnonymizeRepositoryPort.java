package local.sop.datawarehouse.anonymize.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.anonymize.domain.model.Anonymize;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.PersonRef;

public interface AnonymizeRepositoryPort {

    Anonymize save(Anonymize anonymizeLog);

    Optional<Anonymize> findById(AnonymizeId anonymizationId);

    List<Anonymize> findBySearchParams(AnonymizeId anonymizationId, PersonRef personRef);

    List<Anonymize> findAll();

    Boolean compensate(AnonymizeId anonymizationId, SagaOutcome sagaState);
}
