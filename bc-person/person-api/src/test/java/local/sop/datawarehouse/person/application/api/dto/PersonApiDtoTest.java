package local.sop.datawarehouse.person.application.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;

class PersonApiDtoTest {

    @Test
    void shouldExposeCreatePersonCommandValues() {
        UUID organizationRef = UUID.randomUUID();
        CreatePhoneNumberCmd phoneNumber = new CreatePhoneNumberCmd(PhoneUserType.SELF, "12345678");

        CreatePersonCmd cmd = new CreatePersonCmd(
                "John",
                "Doe",
                "john@doe.com",
                organizationRef,
                List.of(phoneNumber)
        );

        assertEquals("John", cmd.firstName());
        assertEquals("Doe", cmd.lastName());
        assertEquals("john@doe.com", cmd.email());
        assertEquals(organizationRef, cmd.organizationRef());
        assertEquals(1, cmd.phoneNumbers().size());
        assertEquals(PhoneUserType.SELF, cmd.phoneNumbers().getFirst().type());
    }

    @Test
    void shouldExposePhoneNumberCommandsAndResponses() {
        UUID phoneNumberId = UUID.randomUUID();

        AddPhoneNumberCmd addCmd = new AddPhoneNumberCmd(PhoneUserType.PARENT, "87654321");
        PhoneNumberResponse response = new PhoneNumberResponse(phoneNumberId, PhoneUserType.PARENT, "87654321");

        assertEquals(PhoneUserType.PARENT, addCmd.type());
        assertEquals("87654321", addCmd.value());
        assertEquals(phoneNumberId, response.id());
        assertEquals(PhoneUserType.PARENT, response.type());
        assertEquals("87654321", response.value());
    }

    @Test
    void shouldExposePersonResponseAndUpdateCommandValues() {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();
        UUID phoneNumberId = UUID.randomUUID();

        PhoneNumberResponse phoneNumber = new PhoneNumberResponse(phoneNumberId, PhoneUserType.SELF, "12345678");
        PersonResponse response = new PersonResponse(
                personId,
                "Jane",
                "Doe",
                "jane@doe.com",
                organizationRef,
                List.of(phoneNumber)
        );
        UpdatePersonCmd updateCmd = new UpdatePersonCmd("Jane", null, "jane@doe.com");

        assertEquals(personId, response.id());
        assertEquals("Jane", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("jane@doe.com", response.email());
        assertEquals(organizationRef, response.organizationRef());
        assertEquals(1, response.phoneNumbers().size());
        assertEquals(phoneNumberId, response.phoneNumbers().getFirst().id());
        assertEquals("Jane", updateCmd.firstName());
        assertNull(updateCmd.lastName());
        assertEquals("jane@doe.com", updateCmd.email());
    }
}