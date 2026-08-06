package local.sop.datawarehouse.person.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;
import local.sop.datawarehouse.person.application.api.dto.PhoneNumberResponse;
import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.FirstName;
import local.sop.datawarehouse.person.domain.model.valueobjects.LastName;
import local.sop.datawarehouse.person.domain.model.valueobjects.OrganizationRef;
import local.sop.datawarehouse.person.domain.model.valueobjects.PersonId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;

class PersonResponseMapperTest {

    @Test
    void shouldMapPersonToResponse() {
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

        PersonResponse response = PersonResponseMapper.toResponse(person);

        assertEquals(personId, response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("john@doe.com", response.email());
        assertEquals(organizationRef, response.organizationRef());
        assertEquals(1, response.phoneNumbers().size());

        PhoneNumberResponse phoneNumber = response.phoneNumbers().getFirst();
        assertEquals(person.getPhoneNumbers().getFirst().getId().value(), phoneNumber.id());
        assertEquals(PhoneUserType.SELF, phoneNumber.type());
        assertEquals("12345678", phoneNumber.value());
    }

    @Test
    void shouldMapPersonWithNoPhoneNumbersToEmptyList() {
        Person person = Person.builder()
                .firstName(new FirstName("John"))
                .lastName(new LastName("Doe"))
                .email(new Email("john@doe.com"))
                .organizationRef(new OrganizationRef(UUID.randomUUID()))
                .build();

        PersonResponse response = PersonResponseMapper.toResponse(person);

        assertNotNull(response.phoneNumbers());
        assertTrue(response.phoneNumbers().isEmpty());
    }
}