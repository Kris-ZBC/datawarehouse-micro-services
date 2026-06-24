package local.sop.sopinfo.person.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import local.sop.sopinfo.person.domain.model.valueobjects.Email;
import local.sop.sopinfo.person.domain.model.valueobjects.FirstName;
import local.sop.sopinfo.person.domain.model.valueobjects.LastName;
import local.sop.sopinfo.person.domain.model.valueobjects.OrganizationRef;
import local.sop.sopinfo.person.domain.model.valueobjects.PersonId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

import org.junit.jupiter.api.Test;

class PersonTest {

    @Test
    void shouldBuildPersonWhenAllRequiredValuesAreProvided() {
        Person person = Person.builder()
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("+45 12 34 56 78"))
                .build();

        assertNotNull(person);
        assertNotNull(person.getId());
        assertEquals("John", person.getFirstName().value());
        assertEquals("Doe", person.getLastName().value());
        assertEquals("john@doe.com", person.getEmail().value());
        assertEquals(1, person.getPhoneNumbers().size());
    }

    @Test
    void shouldGenerateIdWhenIdIsNotProvided() {
        Person person = Person.builder()
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                .build();

        assertNotNull(person.getId());
        assertNotNull(person.getId().value());
    }

    @Test
    void shouldUseProvidedIdWhenIdIsSet() {
        PersonId personId = PersonId.newId();

        Person person = Person.builder()
                .id(personId)
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                .build();

        assertEquals(personId, person.getId());
    }

    @Test
    void shouldReturnNewPersonWhenFirstNameIsChanged() {
        Person original = buildValidPerson();

        Person updated = original.withFirstName(new FirstName("Jane"));

        assertEquals("John", original.getFirstName().value());
        assertEquals("Jane", updated.getFirstName().value());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void shouldReturnNewPersonWhenLastNameIsChanged() {
        Person original = buildValidPerson();

        Person updated = original.withLastName(new LastName("Smith"));

        assertEquals("Doe", original.getLastName().value());
        assertEquals("Smith", updated.getLastName().value());
    }

    @Test
    void shouldReturnNewPersonWhenEmailIsChanged() {
        Person original = buildValidPerson();

        Person updated = original.withEmail(new Email("jane@doe.com"));

        assertEquals("john@doe.com", original.getEmail().value());
        assertEquals("jane@doe.com", updated.getEmail().value());
    }

    @Test
    void shouldReturnNewPersonWhenOrganizationRefIsChanged() {
        Person original = buildValidPerson();
        OrganizationRef newOrganizationRef = new OrganizationRef(UUID.randomUUID());

        Person updated = original.withOrganizationRef(newOrganizationRef);

        assertNotEquals(original.getOrganizationRef(), updated.getOrganizationRef());
        assertEquals(newOrganizationRef, updated.getOrganizationRef());
    }

    @Test
    void shouldRemovePhoneNumber() {
        PhoneNumber firstPhoneNumber = new PhoneNumber(
                PhoneNumberId.newId(),
                PhoneUserType.SELF,
                new PhoneNumberValue("12345678")
        );

        PhoneNumber secondPhoneNumber = new PhoneNumber(
                PhoneNumberId.newId(),
                PhoneUserType.PARENT,
                new PhoneNumberValue("87654321")
        );

        Person person = Person.builder()
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .phoneNumber(firstPhoneNumber)
                .phoneNumber(secondPhoneNumber)
                .build();

        Person updated = person.withRemovedPhoneNumber(firstPhoneNumber.getId());

        assertEquals(2, person.getPhoneNumbers().size());
        assertEquals(1, updated.getPhoneNumbers().size());
        assertEquals(secondPhoneNumber.getId(), updated.getPhoneNumbers().getFirst().getId());
    }

	@Test
	void shouldAllowRemovingLastPhoneNumber() {
		Person person = buildValidPerson();
		DomainId phoneNumberId = person.getPhoneNumbers().getFirst().getId();

		Person updated = person.withRemovedPhoneNumber(phoneNumberId);

		assertEquals(1, person.getPhoneNumbers().size());
		assertEquals(0, updated.getPhoneNumbers().size());
	}

    @Test
    void shouldThrowValidationExceptionWhenFirstNameIsMissingInBuilder() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> Person.builder()
                        .lastName(new LastName("Doe"))
                        .email(new Email("john@doe.com"))
                        .organizationRef(new OrganizationRef(UUID.randomUUID()))
                        .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                        .build()
        );

        assertEquals("person.firstname.blank", exception.getMessage());
    }

	@Test
	void shouldBuildPersonWhenNoPhoneNumbersAreProvided() {
		Person person = Person.builder()
				.firstName(new FirstName("John"))
				.lastName(new LastName("Doe"))
				.email(new Email("john@doe.com"))
				.organizationRef(new OrganizationRef(UUID.randomUUID()))
				.build();

		assertNotNull(person);
		assertNotNull(person.getId());
		assertEquals("John", person.getFirstName().value());
		assertEquals("Doe", person.getLastName().value());
		assertEquals("john@doe.com", person.getEmail().value());
		assertEquals(0, person.getPhoneNumbers().size());
	}

    private Person buildValidPerson() {
        return Person.builder()
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .addPhoneNumber(PhoneUserType.SELF, new PhoneNumberValue("12345678"))
                .build();
    }
}