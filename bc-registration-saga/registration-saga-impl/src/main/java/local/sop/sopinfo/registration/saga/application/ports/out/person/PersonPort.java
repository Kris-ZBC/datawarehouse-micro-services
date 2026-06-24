package local.sop.sopinfo.registration.saga.application.ports.out.person;

import java.util.UUID;

import local.sop.sopinfo.registration.saga.application.api.dto.person.CreatePersonCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.person.PersonResponse;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface PersonPort {
    UUID create(CreatePersonCmd cmd);
    PersonResponse getById(UUID id);
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
