package local.sop.datawarehouse.registration.saga.application.ports.out.person;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePersonCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.PersonResponse;

public interface PersonPort {
    UUID create(CreatePersonCmd cmd);
    PersonResponse getById(UUID id);
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
