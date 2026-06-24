package local.sop.sopinfo.person.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.person.domain.model.Person;
import local.sop.sopinfo.person.domain.model.PhoneNumberDraft;
import local.sop.sopinfo.person.domain.model.valueobjects.Email;
import local.sop.sopinfo.person.domain.model.valueobjects.FirstName;
import local.sop.sopinfo.person.domain.model.valueobjects.LastName;
import local.sop.sopinfo.person.domain.model.valueobjects.OrganizationRef;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;

class PersonDomainServiceTest {

    private final PersonDomainService service = new PersonDomainService();

    @Test
    void shouldCreatePersonWhenPhoneNumbersIsEmpty() {
        Person person = service.create(
                new FirstName("John"),
                new LastName("Doe"),
                new Email("john@doe.com"),
                new OrganizationRef(UUID.randomUUID()),
                List.of()
        );

        assertNotNull(person);
        assertNotNull(person.getId());
        assertEquals("John", person.getFirstName().value());
        assertEquals("Doe", person.getLastName().value());
        assertEquals("john@doe.com", person.getEmail().value());
        assertEquals(0, person.getPhoneNumbers().size());
    }

    @Test
    void shouldCreatePersonWhenPhoneNumbersIsNull() {
        Person person = service.create(
                new FirstName("John"),
                new LastName("Doe"),
                new Email("john@doe.com"),
                new OrganizationRef(UUID.randomUUID()),
                null
        );

        assertNotNull(person);
        assertNotNull(person.getId());
        assertEquals(0, person.getPhoneNumbers().size());
    }
	@Test
	void shouldCreatePersonWithPhoneNumbers() {
		Person person = service.create(
				new FirstName("John"),
				new LastName("Doe"),
				new Email("john@doe.com"),
				new OrganizationRef(UUID.randomUUID()),
				List.of(new PhoneNumberDraft(PhoneUserType.SELF, new PhoneNumberValue("12345678")))
		);

		assertEquals(1, person.getPhoneNumbers().size());
	}
}