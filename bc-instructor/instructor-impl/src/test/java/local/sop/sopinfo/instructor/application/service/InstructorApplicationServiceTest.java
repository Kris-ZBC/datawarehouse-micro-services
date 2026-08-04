package local.sop.sopinfo.instructor.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import local.sop.sopinfo.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.sopinfo.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.sopinfo.instructor.application.api.dto.InstructorResponse;
import local.sop.sopinfo.instructor.domain.model.Instructor;
import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.sopinfo.instructor.domain.model.valueobjects.PersonRef;
import local.sop.sopinfo.instructor.domain.ports.out.InstructorRepositoryPort;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

class InstructorApplicationServiceTest {

    private final StubInstructorRepository repository = new StubInstructorRepository();
    private final InstructorApplicationService service =
            new InstructorApplicationService(repository);

    @Test
    void createInstructor_shouldMapDtoToDomainAndReturnCreatedResponse() {
        UUID personRef = UUID.randomUUID();
        CreateInstructorCmd cmd = new CreateInstructorCmd(personRef);

        CreatedInstructorResponse response = service.createInstructor(cmd);

        assertNotNull(response);
        assertNotNull(response.id());
        assertNotNull(repository.lastSaved);
        assertEquals(personRef, repository.lastSaved.getPersonRef().value());
        assertEquals(response.id(), repository.lastSaved.getId().value());
    }

    @Test
    void createInstructor_shouldThrowValidationException_whenPersonRefIsNull() {
        CreateInstructorCmd cmd = new CreateInstructorCmd(null);

        assertThrows(ValidationException.class, () -> service.createInstructor(cmd));
    }

    @Test
    void findAll_shouldReturnMappedResponses() {
        Instructor first = Instructor.builder()
                .id(InstructorId.of(UUID.randomUUID()))
                .personRef(PersonRef.of(UUID.randomUUID()))
                .build();

        Instructor second = Instructor.builder()
                .id(InstructorId.of(UUID.randomUUID()))
                .personRef(PersonRef.of(UUID.randomUUID()))
                .build();

        repository.items = List.of(first, second);

        List<InstructorResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals(first.getId().value(), responses.get(0).id());
        assertEquals(first.getPersonRef().value(), responses.get(0).personRef());
        assertEquals(second.getId().value(), responses.get(1).id());
        assertEquals(second.getPersonRef().value(), responses.get(1).personRef());
    }

    @Test
    void findById_shouldReturnMappedResponse_whenInstructorExists() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        Instructor instructor = Instructor.builder()
                .id(InstructorId.of(id))
                .personRef(PersonRef.of(personRef))
                .build();

        repository.singleResult = instructor;

        InstructorResponse response = service.findById(id);

        assertEquals(id, response.id());
        assertEquals(personRef, response.personRef());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenInstructorDoesNotExist() {
        UUID id = UUID.randomUUID();
        repository.singleResult = null;

        assertThrows(NotFoundException.class, () -> service.findById(id));
    }

    private static class StubInstructorRepository implements InstructorRepositoryPort {
        private Instructor lastSaved;
        private List<Instructor> items = List.of();
        private Instructor singleResult;
        private Boolean compensateResult = false;


        @Override
        public Instructor save(Instructor instructor) {
            this.lastSaved = instructor;
            return instructor;
        }

        @Override
        public List<Instructor> findAll() {
            return items;
        }

        @Override
        public Optional<Instructor> findById(InstructorId id) {
            if (singleResult == null) {
                return Optional.empty();
            }
            return Optional.of(singleResult);
        }

        @Override
        public Boolean compensate(InstructorId id, SagaOutcome sagaState) {
            return compensateResult;
        }
        // @Override
        // public Boolean compensate(InstructorId id, SagaOutcome sagaState) {
        //     throw new UnsupportedOperationException("Unimplemented method 'compensate'");
    }



    @Test
    void compensate_should_return_idempotent_false_when_instructor_not_found() {
        UUID id = UUID.randomUUID();
        repository.singleResult = null;

        ResponseCompensated result = service.compensate(id, Instructor.class, SagaOutcome.COMPENSATE);

        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());
    }

    @Test
    void compensate_should_return_compensated_true_when_compensation_succeeds() {
        UUID id = UUID.randomUUID();
        repository.singleResult = Instructor.builder().personRef(PersonRef.of(UUID.randomUUID())).build();
        repository.compensateResult = true;

        ResponseCompensated result = service.compensate(id, Instructor.class, SagaOutcome.COMPENSATE);

        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());
    }

    @Test
    void compensate_should_return_idempotent_true_when_compensation_fails() {
        UUID id = UUID.randomUUID();
        repository.singleResult = Instructor.builder().personRef(PersonRef.of(UUID.randomUUID())).build();
        repository.compensateResult = false;

        ResponseCompensated result = service.compensate(id, Instructor.class, SagaOutcome.COMPENSATE);

        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.success());
    }
}