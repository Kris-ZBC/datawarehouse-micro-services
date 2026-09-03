package local.sop.datawarehouse.person.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.sharedlib.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.person.application.api.dto.AddPhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.CreatePersonCmd;
import local.sop.datawarehouse.person.application.api.dto.CreatePhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;
import local.sop.datawarehouse.person.application.api.dto.PhoneNumberResponse;
import local.sop.datawarehouse.person.application.api.dto.UpdatePersonCmd;
import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.PhoneNumber;
import local.sop.datawarehouse.person.domain.model.PhoneNumberDraft;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.FirstName;
import local.sop.datawarehouse.person.domain.model.valueobjects.LastName;
import local.sop.datawarehouse.person.domain.model.valueobjects.OrganizationRef;
import local.sop.datawarehouse.person.domain.model.valueobjects.PersonId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.datawarehouse.person.domain.ports.out.PersonRepositoryPort;
import local.sop.datawarehouse.person.domain.service.PersonDomain;

class PersonApplicationServiceTest {

    @Test
    void shouldCreatePersonWhenPhoneNumbersIsNull() {
        CapturingPersonDomain domain = new CapturingPersonDomain();
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        PersonApplicationService service = new PersonApplicationService(domain, repository);

        UUID organizationRef = UUID.randomUUID();

        CreatePersonCmd cmd = new CreatePersonCmd(
                "John",
                "Doe",
                "john@doe.com",
                organizationRef,
                null
        );

        UUID createdId = service.create(cmd);

        assertNotNull(createdId);
        assertEquals(0, domain.receivedPhoneNumbers.size());
        assertEquals(1, repository.savedPersons.size());
        assertEquals(createdId, repository.savedPersons.getFirst().getId().value());
    }

    @Test
    void shouldCreatePersonWithPhoneNumbersFromCommand() {
        CapturingPersonDomain domain = new CapturingPersonDomain();
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        PersonApplicationService service = new PersonApplicationService(domain, repository);

        UUID organizationRef = UUID.randomUUID();

        CreatePersonCmd cmd = new CreatePersonCmd(
                "John",
                "Doe",
                "john@doe.com",
                organizationRef,
                List.of(
                        new CreatePhoneNumberCmd(PhoneUserType.SELF, "12345678"),
                        new CreatePhoneNumberCmd(PhoneUserType.PARENT, "87654321")
                )
        );

        UUID createdId = service.create(cmd);

        assertNotNull(createdId);
        assertEquals(2, domain.receivedPhoneNumbers.size());
        assertEquals(PhoneUserType.SELF, domain.receivedPhoneNumbers.get(0).type());
        assertEquals("12345678", domain.receivedPhoneNumbers.get(0).value().value());
        assertEquals(PhoneUserType.PARENT, domain.receivedPhoneNumbers.get(1).type());
        assertEquals("87654321", domain.receivedPhoneNumbers.get(1).value().value());
        assertEquals(2, repository.savedPersons.getFirst().getPhoneNumbers().size());
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        CapturingPersonDomain domain = new CapturingPersonDomain();
        InMemoryPersonRepository repository = new InMemoryPersonRepository();

        Person existingPerson = Person.builder()
                .firstName(new FirstName("Existing"))
                .lastName(new LastName("User"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                .build();

        repository.personByEmail = Optional.of(existingPerson);

        PersonApplicationService service = new PersonApplicationService(domain, repository);

        CreatePersonCmd cmd = new CreatePersonCmd(
                "John",
                "Doe",
                "john@doe.com",
                UUID.randomUUID(),
                null
        );

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.create(cmd)
        );

        assertEquals("person.email.exists", exception.getMessage());
        assertEquals(0, repository.savedPersons.size());
    }

    @Test
    void shouldReturnPersonResponseWhenFindByIdExists() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        Person person = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(organizationRef))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(person);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        Optional<PersonResponse> result = service.findById(personId);

        assertTrue(result.isPresent());

        PersonResponse response = result.get();

        assertEquals(personId, response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("john@doe.com", response.email());
        assertEquals(organizationRef, response.organizationRef());
    }

    @Test
    void shouldThrowNotFoundWhenFindByIdDoesNotExist() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        Optional<PersonResponse> result =
            service.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllPersons() {
        InMemoryPersonRepository repository = new InMemoryPersonRepository();

        repository.savedPersons.add(
                Person.builder()
                        .firstName(new FirstName("John"))
                        .lastName(new LastName("Doe"))
                        .email(new Email("john@doe.com"))
                        .organizationRef(new OrganizationRef(UUID.randomUUID()))
                        .build()
        );

        repository.savedPersons.add(
                Person.builder()
                        .firstName(new FirstName("Jane"))
                        .lastName(new LastName("Doe"))
                        .email(new Email("jane@doe.com"))
                        .organizationRef(new OrganizationRef(UUID.randomUUID()))
                        .build()
        );

        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                repository
        );

        List<PersonResponse> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldSearchByNameUsingTrimmedValue() {
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(
                Person.builder()
                        .firstName(new FirstName("John"))
                        .lastName(new LastName("Doe"))
                        .email(new Email("john@doe.com"))
                        .organizationRef(new OrganizationRef(UUID.randomUUID()))
                        .build()
        );

        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                repository
        );

        List<PersonResponse> result = service.searchByName("  john  ");

        assertEquals(1, result.size());
        assertEquals("john", repository.lastSearchName);
        assertEquals("John", result.getFirst().firstName());
    }

    @Test
    void shouldThrowValidationWhenSearchNameIsNull() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.searchByName(null)
        );

        assertEquals("person.search.name.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationWhenSearchNameIsBlank() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.searchByName("   ")
        );

        assertEquals("person.search.name.blank", exception.getMessage());
    }

    @Test
    void shouldUpdateRequestedFields() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        Person existingPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(organizationRef))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(existingPerson);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        PersonResponse response = service.update(
                personId,
                new UpdatePersonCmd("Jane", "Smith", "jane@doe.com")
        );

        assertEquals(personId, response.id());
        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("jane@doe.com", response.email());
        assertEquals("Jane", repository.savedPersons.getFirst().getFirstName().value());
    }

    @Test
    void shouldAllowUpdateWhenEmailIsUnchanged() {
        UUID personId = UUID.randomUUID();

        Person existingPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(existingPerson);
        repository.personByEmail = Optional.of(existingPerson);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        PersonResponse response = service.update(
                personId,
                new UpdatePersonCmd("Johnny", null, "john@doe.com")
        );

        assertEquals("Johnny", response.firstName());
        assertEquals("john@doe.com", response.email());
    }

    @Test
    void shouldThrowValidationWhenUpdateCommandIsNull() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.update(UUID.randomUUID(), null)
        );

        assertEquals("person.update.empty", exception.getMessage());
    }

    @Test
    void shouldThrowValidationWhenUpdateCommandHasNoChanges() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.update(UUID.randomUUID(), new UpdatePersonCmd(null, null, null))
        );

        assertEquals("person.update.empty", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingPerson() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.update(UUID.randomUUID(), new UpdatePersonCmd("Jane", null, null))
        );

        assertEquals("person.not.found", exception.getMessage());
    }

    @Test
    void shouldThrowConflictWhenUpdatingEmailToExistingEmail() {
        UUID personId = UUID.randomUUID();

        Person existingPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build();

        Person otherPerson = Person.builder()
                .firstName(new FirstName("Jane"))
                .lastName(new LastName("Smith"))
                .email(new Email("jane@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(existingPerson);
        repository.personByEmail = Optional.of(otherPerson);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.update(personId, new UpdatePersonCmd(null, null, "jane@doe.com"))
        );

        assertEquals("person.email.exists", exception.getMessage());
    }

    @Test
    void shouldAddPhoneNumberToExistingPerson() {
        UUID personId = UUID.randomUUID();

        Person existingPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(existingPerson);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        PhoneNumberResponse response = service.addPhoneNumber(
                personId,
                new AddPhoneNumberCmd(PhoneUserType.SELF, "12345678")
        );

        assertEquals(PhoneUserType.SELF, response.type());
        assertEquals("12345678", response.value());
        assertEquals(1, repository.savedPersons.size());
        assertEquals(1, repository.savedPersons.getFirst().getPhoneNumbers().size());
    }

    @Test
    void shouldThrowNotFoundWhenAddPhoneNumberToMissingPerson() {
        PersonApplicationService service = new PersonApplicationService(
                new CapturingPersonDomain(),
                new InMemoryPersonRepository()
        );

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.addPhoneNumber(
                        UUID.randomUUID(),
                        new AddPhoneNumberCmd(PhoneUserType.SELF, "12345678"))
        );

        assertEquals("person.not.found", exception.getMessage());
    }

    @Test
    void shouldRemovePhoneNumberFromExistingPerson() {
        UUID personId = UUID.randomUUID();
        PhoneNumberId phoneNumberId = PhoneNumberId.newId();

        Person existingPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .phoneNumber(new PhoneNumber(
                        phoneNumberId,
                        PhoneUserType.SELF,
                        new PhoneNumberValue("12345678")))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(existingPerson);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        service.removePhoneNumber(personId, phoneNumberId.value());

        assertEquals(1, repository.savedPersons.size());
        assertEquals(0, repository.savedPersons.getFirst().getPhoneNumbers().size());
    }

    @Test
    void shouldThrowNotFoundWhenRemoveUnknownPhoneNumber() {
        UUID personId = UUID.randomUUID();

        Person existingPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build();

        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(existingPerson);

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.removePhoneNumber(personId, UUID.randomUUID())
        );

        assertEquals("person.phonenumber.not.found", exception.getMessage());
    }

    private static final class CapturingPersonDomain implements PersonDomain {
        private List<PhoneNumberDraft> receivedPhoneNumbers = List.of();

        @Override
        public Person create(
                FirstName firstName,
                LastName lastName,
                Email email,
                OrganizationRef organizationRef,
                List<PhoneNumberDraft> phoneNumbers
        ) {
            this.receivedPhoneNumbers = phoneNumbers == null ? List.of() : List.copyOf(phoneNumbers);

            Person.Builder builder = Person.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .organizationRef(organizationRef);

            for (PhoneNumberDraft phoneNumberDraft : this.receivedPhoneNumbers) {
                builder.addPhoneNumber(phoneNumberDraft.type(), phoneNumberDraft.value());
            }

            return builder.build();
        }
    }

    private static final class InMemoryPersonRepository implements PersonRepositoryPort {
        private final List<Person> savedPersons = new ArrayList<>();
        private Optional<Person> personByEmail = Optional.empty();
        private String lastSearchName;
        private Boolean compensateResult = false;

        @Override
        public Person save(Person person) {
            savedPersons.removeIf(existing -> existing.getId().equals(person.getId()));
            savedPersons.add(person);
            return person;
        }

        @Override
        public Optional<Person> findById(PersonId id) {
            return savedPersons.stream()
                    .filter(person -> person.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Person> findAll() {
            return List.copyOf(savedPersons);
        }

        @Override
        public List<Person> searchByName(String name) {
            this.lastSearchName = name;

            return savedPersons.stream()
                    .filter(person ->
                            person.getFirstName().value().toLowerCase().contains(name.toLowerCase())
                                    || person.getLastName().value().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        @Override
        public Optional<Person> findByEmail(Email email) {
            return savedPersons.stream()
                    .filter(person -> person.getEmail().equals(email))
                    .findFirst()
                    .or(() -> personByEmail.filter(person -> person.getEmail().equals(email)));
        }

        @Override
        public Boolean compensate(PersonId id, SagaOutcome state) {
                return compensateResult;
                // throw new UnsupportedOperationException("Unimplemented method 'compensate'");
        }        
    }

    @Test
        void compensate_should_return_idempotent_false_when_person_not_found() {
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        ResponseCompensated result = service.compensate(UUID.randomUUID(), Person.class, SagaOutcome.COMPENSATE);

        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());
        }

        @Test
        void compensate_should_return_compensated_true_when_compensation_succeeds() {
        UUID personId = UUID.randomUUID();
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        repository.savedPersons.add(Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build());
        repository.compensateResult = true;

        PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

        ResponseCompensated result = service.compensate(personId, Person.class, SagaOutcome.COMPENSATE);

        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());
        }

        @Test
        void compensate_should_return_idempotent_true_when_compensation_fails() {
                UUID personId = UUID.randomUUID();
                InMemoryPersonRepository repository = new InMemoryPersonRepository();
                repository.savedPersons.add(Person.builder()
                        .id(new PersonId(personId))
                        .firstName(new FirstName("John"))
                        .lastName(new LastName("Doe"))
                        .email(new Email("john@doe.com"))
                        .organizationRef(new OrganizationRef(UUID.randomUUID()))
                        .build());
                repository.compensateResult = false;

                PersonApplicationService service = new PersonApplicationService(new CapturingPersonDomain(), repository);

                ResponseCompensated result = service.compensate(personId, Person.class, SagaOutcome.COMPENSATE);

                assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
                assertTrue(result.success());
        }
}