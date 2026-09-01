package local.sop.datawarehouse.person.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.PhoneNumber;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.FirstName;
import local.sop.datawarehouse.person.domain.model.valueobjects.LastName;
import local.sop.datawarehouse.person.domain.model.valueobjects.OrganizationRef;
import local.sop.datawarehouse.person.domain.model.valueobjects.PersonId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.application.name=person-impl",
        "sop.application.name=datawarehouse"
})
class PersonRepositoryJpaAdapterTest {

    @Autowired
    private PersonSpringDataRepository personSpringDataRepository;

    private PersonRepositoryJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PersonRepositoryJpaAdapter(personSpringDataRepository, new PersonJpaMapper());
    }

    @Test
    void shouldSavePerson() {
        UUID personId = UUID.randomUUID();

        Person person = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                .build();

        Person saved = adapter.save(person);

        assertEquals(personId, saved.getId().value());
        assertEquals("John", saved.getFirstName().value());
        assertEquals(1, saved.getPhoneNumbers().size());

        Optional<PersonEntity> persistedEntity = personSpringDataRepository.findById(personId);
        assertTrue(persistedEntity.isPresent());
        assertEquals("john@doe.com", persistedEntity.get().getEmail());
        assertEquals(1, persistedEntity.get().getPhoneNumbers().size());
    }

    @Test
    void shouldUpdateExistingPersonPhoneNumbers() {
        UUID personId = UUID.randomUUID();
        PhoneNumberId originalPhoneNumberId = PhoneNumberId.newId();
        PhoneNumberId newPhoneNumberId = PhoneNumberId.newId();

        Person initialPerson = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .phoneNumber(new PhoneNumber(originalPhoneNumberId, PhoneUserType.SELF, new PhoneNumberValue("12345678")))
                .build();

        adapter.save(initialPerson);

        Person updatedPerson = initialPerson
                .withRemovedPhoneNumber(originalPhoneNumberId)
                .withAddedPhoneNumber(new PhoneNumber(newPhoneNumberId, PhoneUserType.PARENT, new PhoneNumberValue("87654321")));

        adapter.save(updatedPerson);

        PersonEntity persistedEntity = personSpringDataRepository.findById(personId).orElseThrow();

        assertEquals(1, persistedEntity.getPhoneNumbers().size());
        assertEquals(newPhoneNumberId.value(), persistedEntity.getPhoneNumbers().getFirst().getId());
        assertEquals("87654321", persistedEntity.getPhoneNumbers().getFirst().getPhoneNumber());
    }

    @Test
    void shouldFindPersonById() {
        UUID personId = UUID.randomUUID();

        PersonEntity entity = new PersonEntity(
                personId,
                "Jane",
                "Doe",
                "jane@doe.com",
                UUID.randomUUID()
        );
        entity.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.PARENT,
                "87654321"
        ));

        personSpringDataRepository.save(entity);

        Optional<Person> result = adapter.findById(new PersonId(personId));

        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName().value());
        assertEquals("jane@doe.com", result.get().getEmail().value());
        assertEquals(1, result.get().getPhoneNumbers().size());
    }

    @Test
    void shouldFindPersonByEmail() {
        UUID personId = UUID.randomUUID();

        PersonEntity entity = new PersonEntity(
                personId,
                "Alice",
                "Smith",
                "alice@doe.com",
                UUID.randomUUID()
        );
        entity.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.SELF,
                "11112222"
        ));

        personSpringDataRepository.save(entity);

        Optional<Person> result = adapter.findByEmail(new Email("alice@doe.com"));

        assertTrue(result.isPresent());
        assertEquals(personId, result.get().getId().value());
        assertEquals("Alice", result.get().getFirstName().value());
    }

    @Test
    void shouldFindAllPersons() {
        PersonEntity first = new PersonEntity(
                UUID.randomUUID(),
                "John",
                "Doe",
                "john@doe.com",
                UUID.randomUUID()
        );
        first.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.SELF,
                "12345678"
        ));

        PersonEntity second = new PersonEntity(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "jane@doe.com",
                UUID.randomUUID()
        );
        second.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.PARENT,
                "87654321"
        ));

        personSpringDataRepository.saveAll(List.of(first, second));

        List<Person> result = adapter.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldSearchByNameIgnoringCase() {
        PersonEntity match = new PersonEntity(
                UUID.randomUUID(),
                "John",
                "Doe",
                "john@doe.com",
                UUID.randomUUID()
        );
        match.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.SELF,
                "12345678"
        ));

        PersonEntity other = new PersonEntity(
                UUID.randomUUID(),
                "Alice",
                "Smith",
                "alice@doe.com",
                UUID.randomUUID()
        );
        other.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.PARENT,
                "87654321"
        ));

        personSpringDataRepository.saveAll(List.of(match, other));

        List<Person> result = adapter.searchByName("joH");

        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().getFirstName().value());
    }

    @Test
    void shouldReturnEmptyWhenFindByIdDoesNotExist() {
        Optional<Person> result = adapter.findById(new PersonId(UUID.randomUUID()));

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenFindByEmailDoesNotExist() {
        Optional<Person> result = adapter.findByEmail(new Email("missing@doe.com"));

        assertFalse(result.isPresent());
    }

    @Test
    void shouldSearchByLastNameIgnoringCase() {
        PersonEntity match = new PersonEntity(
                UUID.randomUUID(),
                "Alice",
                "Johnson",
                "alice@doe.com",
                UUID.randomUUID()
        );
        match.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.SELF,
                "12345678"
        ));

        PersonEntity other = new PersonEntity(
                UUID.randomUUID(),
                "Bob",
                "Smith",
                "bob@doe.com",
                UUID.randomUUID()
        );
        other.addPhoneNumber(new PhoneNumberEntity(
                UUID.randomUUID(),
                PhoneUserType.PARENT,
                "87654321"
        ));

        personSpringDataRepository.saveAll(List.of(match, other));

        List<Person> result = adapter.searchByName("JOHN");

        assertEquals(1, result.size());
        assertEquals("Johnson", result.getFirst().getLastName().value());
    }


    @Test
        void compensate_should_throw_when_wrong_state() {
        UUID id = UUID.randomUUID();

        assertThrows(ConflictException.class, () ->
                adapter.compensate(new PersonId(id), SagaOutcome.SUCCEEDED)
        );
        }

        @Test
        void compensate_should_return_false_when_person_not_found() {
        UUID id = UUID.randomUUID();

        Boolean result = adapter.compensate(new PersonId(id), SagaOutcome.COMPENSATE);

        assertFalse(result);
        }

        @Test
        void compensate_should_return_true_when_person_deleted_successfully() {
        UUID personId = UUID.randomUUID();

        PersonEntity entity = new PersonEntity(
                personId,
                "John",
                "Doe",
                "john@doe.com",
                UUID.randomUUID()
        );

        personSpringDataRepository.save(entity);

        Boolean result = adapter.compensate(new PersonId(personId), SagaOutcome.COMPENSATE);

        assertTrue(result);
        }
}