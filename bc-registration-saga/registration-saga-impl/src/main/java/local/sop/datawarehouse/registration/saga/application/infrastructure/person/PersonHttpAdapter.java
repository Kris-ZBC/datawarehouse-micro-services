package local.sop.datawarehouse.registration.saga.application.infrastructure.person;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePersonCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.PersonResponse;
import local.sop.datawarehouse.registration.saga.application.ports.out.person.PersonPort;

public class PersonHttpAdapter implements PersonPort {

    private final RestClient person;

    public PersonHttpAdapter(@Qualifier("person") RestClient person) {
        this.person = person;
    }
    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return person.put()
            .uri("/internal/persons/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }
    @Override
    public UUID create(CreatePersonCmd cmd) {
        return person.post()
            .uri("/internal/persons")
            .body(cmd)
            .retrieve()
            .body(UUID.class);
    }

    @Override
    public PersonResponse getById(UUID id) {
        return person.get()
            .uri("/internal/persons/{id}", id)
            .retrieve()
            .body(PersonResponse.class);
    }   

}
