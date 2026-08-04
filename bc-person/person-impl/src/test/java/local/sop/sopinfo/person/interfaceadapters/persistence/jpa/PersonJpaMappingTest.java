package local.sop.sopinfo.person.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.person.domain.model.Person;
import local.sop.sopinfo.person.domain.model.PhoneNumber;
import local.sop.sopinfo.person.domain.model.valueobjects.Email;
import local.sop.sopinfo.person.domain.model.valueobjects.FirstName;
import local.sop.sopinfo.person.domain.model.valueobjects.LastName;
import local.sop.sopinfo.person.domain.model.valueobjects.OrganizationRef;
import local.sop.sopinfo.person.domain.model.valueobjects.PersonId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;

class PersonJpaMappingTest {

    private final PersonJpaMapper mapper = new PersonJpaMapper();

    @Test
    void shouldMapDomainToEntity() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        Person person = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(organizationRef))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                .build();

        PersonEntity entity = mapper.toEntity(person);

        assertNotNull(entity);
        assertEquals(personId, entity.getId());
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals("john@doe.com", entity.getEmail());
        assertEquals(organizationRef, entity.getOrganizationRef());
        assertEquals(1, entity.getPhoneNumbers().size());

        PhoneNumberEntity phoneNumberEntity = entity.getPhoneNumbers().getFirst();
        assertEquals(PhoneUserType.SELF, phoneNumberEntity.getType());
        assertEquals("12345678", phoneNumberEntity.getPhoneNumber());
        assertEquals(entity, phoneNumberEntity.getPerson());
    }

    @Test
    void shouldMapEntityToDomain() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();
        UUID phoneNumberId = UUID.randomUUID();

        PersonEntity entity = new PersonEntity(
                personId,
                "Jane",
                "Doe",
                "jane@doe.com",
                organizationRef
        );

        PhoneNumberEntity phoneNumberEntity = new PhoneNumberEntity(
                phoneNumberId,
                PhoneUserType.PARENT,
                "87654321"
        );

        entity.addPhoneNumber(phoneNumberEntity);

        Person person = mapper.toDomain(entity);

        assertNotNull(person);
        assertEquals(personId, person.getId().value());
        assertEquals("Jane", person.getFirstName().value());
        assertEquals("Doe", person.getLastName().value());
        assertEquals("jane@doe.com", person.getEmail().value());
        assertEquals(organizationRef, person.getOrganizationRef().value());
        assertEquals(1, person.getPhoneNumbers().size());
        assertEquals(phoneNumberId, person.getPhoneNumbers().getFirst().getId().value());
        assertEquals(PhoneUserType.PARENT, person.getPhoneNumbers().getFirst().getType());
        assertEquals("87654321", person.getPhoneNumbers().getFirst().getValue().value());
    }

    @Test
    void shouldMapEntityWithoutPhoneNumbersToDomain() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        PersonEntity entity = new PersonEntity(
                personId,
                "Jane",
                "Doe",
                "jane@doe.com",
                organizationRef
        );

        Person person = mapper.toDomain(entity);

        assertEquals(personId, person.getId().value());
        assertEquals(0, person.getPhoneNumbers().size());
    }

    @Test
    void shouldCopyDomainIntoExistingEntityAndReplacePhoneNumbers() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();
        UUID newPhoneNumberId = UUID.randomUUID();

        Person person = Person.builder()
                .id(new PersonId(personId))
                .firstName(new FirstName("Updated"))
                .lastName(new LastName("Person"))
                .email(new Email("updated@doe.com"))
                .organizationRef(new OrganizationRef(organizationRef))
                .phoneNumber(new PhoneNumber(
                        new PhoneNumberId(newPhoneNumberId),
                        PhoneUserType.GUARDIAN,
                        new PhoneNumberValue("11112222")))
                .build();

        PersonEntity entity = new PersonEntity(
                personId,
                "Old",
                "Name",
                "old@doe.com",
                UUID.randomUUID()
        );
        entity.addPhoneNumber(new PhoneNumberEntity(UUID.randomUUID(), PhoneUserType.SELF, "99998888"));

        mapper.copyIntoEntity(person, entity);

        assertEquals(personId, entity.getId());
        assertEquals("Updated", entity.getFirstName());
        assertEquals("Person", entity.getLastName());
        assertEquals("updated@doe.com", entity.getEmail());
        assertEquals(organizationRef, entity.getOrganizationRef());
        assertEquals(1, entity.getPhoneNumbers().size());
        assertEquals(newPhoneNumberId, entity.getPhoneNumbers().getFirst().getId());
        assertEquals(entity, entity.getPhoneNumbers().getFirst().getPerson());
        assertEquals(PhoneUserType.GUARDIAN, entity.getPhoneNumbers().getFirst().getType());
        assertEquals("11112222", entity.getPhoneNumbers().getFirst().getPhoneNumber());
    }
}