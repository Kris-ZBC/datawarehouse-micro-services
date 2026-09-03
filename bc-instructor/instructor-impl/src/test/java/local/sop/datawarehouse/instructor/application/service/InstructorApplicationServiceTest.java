package local.sop.datawarehouse.instructor.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.datawarehouse.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.datawarehouse.instructor.application.api.dto.InstructorResponse;
import local.sop.datawarehouse.instructor.domain.model.Instructor;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.InstructorId;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.instructor.domain.ports.out.InstructorRepositoryPort;
import local.sop.datawarehouse.sharedlib.login.WellKnownLogins;

class InstructorApplicationServiceTest {

    private final StubInstructorRepository repository = new StubInstructorRepository();
    private final InstructorApplicationService service =
            new InstructorApplicationService(repository);

    private static final UUID ORDINARY_CALLER = UUID.randomUUID();

    @Test
    void createInstructor_shouldMapDtoToDomainAndReturnCreatedResponse() {
        UUID personRef = UUID.randomUUID();
        // CHANGED: CreateInstructorCmd now takes callerLoginId too.
        CreateInstructorCmd cmd = new CreateInstructorCmd(personRef, ORDINARY_CALLER);

        CreatedInstructorResponse response = service.createInstructor(cmd);

        assertNotNull(response);
        assertNotNull(response.id());
        assertNotNull(repository.lastSaved);
        assertEquals(personRef, repository.lastSaved.getPersonRef().value());
        assertEquals(response.id(), repository.lastSaved.getId().value());
    }

    @Test
    void createInstructor_shouldThrowValidationException_whenPersonRefIsNull() {
        CreateInstructorCmd cmd = new CreateInstructorCmd(null, ORDINARY_CALLER);

        assertThrows(ValidationException.class, () -> service.createInstructor(cmd));
    }

    // NEW: coverage for the tech-user guard — see
    // InstructorApplicationService's Javadoc on createInstructor() for
    // why this lives here rather than in a separate module.
    @Nested
    class TechUserGuard {

        @Test
        void shouldAllow_whenTechUserAndNoInstructorExistsYet() {
            repository.singleResult = null; // no instructor exists yet
            CreateInstructorCmd cmd = new CreateInstructorCmd(UUID.randomUUID(), WellKnownLogins.TECH_USER_ID);

            CreatedInstructorResponse response = service.createInstructor(cmd);

            assertNotNull(response);
            assertNotNull(repository.lastSaved);
        }

        @Test
        void shouldReject_whenTechUserAndInstructorAlreadyExists() {
            repository.singleResult = Instructor.builder().personRef(PersonRef.of(UUID.randomUUID())).build();
            CreateInstructorCmd cmd = new CreateInstructorCmd(UUID.randomUUID(), WellKnownLogins.TECH_USER_ID);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.createInstructor(cmd));

            assertEquals("instructor.techuser.notallowed", ex.getMessage());
            // Nothing should have been saved — the guard must run before
            // the actual creation, not after.
            assertNull(repository.lastSaved);
        }

        @Test
        void shouldAllow_whenOrdinaryCallerAndInstructorAlreadyExists() {
            repository.singleResult = Instructor.builder().personRef(PersonRef.of(UUID.randomUUID())).build();
            CreateInstructorCmd cmd = new CreateInstructorCmd(UUID.randomUUID(), ORDINARY_CALLER);

            CreatedInstructorResponse response = service.createInstructor(cmd);

            assertNotNull(response);
            assertNotNull(repository.lastSaved);
        }

        @Test
        void shouldAllow_whenCallerLoginIdIsNull() {
            // gw-admin doesn't yet resolve the authenticated caller's
            // identity — until that exists, callerLoginId arrives null,
            // and a null can never equal TECH_USER_ID, so the guard
            // simply never fires. Not a regression, see
            // InstructorApplicationService's Javadoc.
            repository.singleResult = Instructor.builder().personRef(PersonRef.of(UUID.randomUUID())).build();
            CreateInstructorCmd cmd = new CreateInstructorCmd(UUID.randomUUID(), null);

            CreatedInstructorResponse response = service.createInstructor(cmd);

            assertNotNull(response);
        }
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

    // NEW: coverage for findByPersonRef(), added to the port for
    // login-saga's role resolution.
    @Test
    void findByPersonRef_shouldReturnResponse_whenInstructorExists() {
        UUID personRef = UUID.randomUUID();
        Instructor instructor = Instructor.builder()
                .id(InstructorId.of(UUID.randomUUID()))
                .personRef(PersonRef.of(personRef))
                .build();
        repository.byPersonRefResult = instructor;

        Optional<InstructorResponse> response = service.findByPersonRef(personRef);

        assertTrue(response.isPresent());
        assertEquals(personRef, response.get().personRef());
    }

    @Test
    void findByPersonRef_shouldReturnEmpty_whenNoInstructorForThatPerson() {
        // Not being an instructor is a normal outcome here (they might
        // be an apprentice instead) — no exception expected.
        Optional<InstructorResponse> response = service.findByPersonRef(UUID.randomUUID());

        assertTrue(response.isEmpty());
    }

    private static class StubInstructorRepository implements InstructorRepositoryPort {
        private Instructor lastSaved;
        private List<Instructor> items = List.of();
        private Instructor singleResult;
        private Instructor byPersonRefResult;
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

        // NEW: required by InstructorRepositoryPort — without this,
        // this stub doesn't compile at all (a concrete class can't
        // partially implement an interface).
        @Override
        public Optional<Instructor> findByPersonRef(UUID personRef) {
            return Optional.ofNullable(byPersonRefResult);
        }

        @Override
        public Boolean compensate(InstructorId id, SagaOutcome sagaState) {
            return compensateResult;
        }

        @Override
        public Boolean exists() {
            return singleResult != null? true : false;
        }
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