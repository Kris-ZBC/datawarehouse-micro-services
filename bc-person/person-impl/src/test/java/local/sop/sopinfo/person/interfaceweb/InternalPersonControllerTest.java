package local.sop.sopinfo.person.interfaceweb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.sopinfo.person.application.api.PersonDirectory;
import local.sop.sopinfo.person.application.api.dto.AddPhoneNumberCmd;
import local.sop.sopinfo.person.application.api.dto.CreatePersonCmd;
import local.sop.sopinfo.person.application.api.dto.CreatePhoneNumberCmd;
import local.sop.sopinfo.person.application.api.dto.PersonResponse;
import local.sop.sopinfo.person.application.api.dto.PhoneNumberResponse;
import local.sop.sopinfo.person.application.api.dto.RemovePhoneNumberCmd;
import local.sop.sopinfo.person.application.api.dto.UpdatePersonCmd;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

class InternalPersonControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private StubPersonDirectory personDirectory;

    @BeforeEach
    void setUp() {
        personDirectory = new StubPersonDirectory();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new InternalPersonController(personDirectory)).build();
    }

    @Test
    void shouldCreatePerson() throws Exception {
        UUID createdId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        personDirectory.createResult = createdId;

        CreatePersonCmd cmd = new CreatePersonCmd(
                "John",
                "Doe",
                "john@doe.com",
                organizationRef,
                List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "12345678"))
        );

        mockMvc.perform(post("/internal/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + createdId + "\""));

        assertNotNull(personDirectory.receivedCreateCmd);
        assertEquals("John", personDirectory.receivedCreateCmd.firstName());
        assertEquals("Doe", personDirectory.receivedCreateCmd.lastName());
        assertEquals("john@doe.com", personDirectory.receivedCreateCmd.email());
        assertEquals(organizationRef, personDirectory.receivedCreateCmd.organizationRef());
        assertEquals(1, personDirectory.receivedCreateCmd.phoneNumbers().size());
    }

    @Test
    void shouldCreatePersonWithoutPhoneNumbers() throws Exception {
        UUID createdId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        personDirectory.createResult = createdId;

        CreatePersonCmd cmd = new CreatePersonCmd(
                "John",
                "Doe",
                "john@doe.com",
                organizationRef,
                null
        );

        mockMvc.perform(post("/internal/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + createdId + "\""));

        assertNotNull(personDirectory.receivedCreateCmd);
        assertEquals("John", personDirectory.receivedCreateCmd.firstName());
        assertEquals("Doe", personDirectory.receivedCreateCmd.lastName());
        assertEquals("john@doe.com", personDirectory.receivedCreateCmd.email());
        assertEquals(organizationRef, personDirectory.receivedCreateCmd.organizationRef());
        assertEquals(null, personDirectory.receivedCreateCmd.phoneNumbers());
    }

    @Test
    void shouldFindAllPersons() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();
        UUID phoneNumberId = UUID.randomUUID();

        personDirectory.findAllResult = List.of(
                new PersonResponse(
                        personId,
                        "John",
                        "Doe",
                        "john@doe.com",
                        organizationRef,
                        List.of(new PhoneNumberResponse(phoneNumberId, PhoneUserType.SELF, "12345678"))
                )
        );

        mockMvc.perform(get("/internal/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(personId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john@doe.com"))
                .andExpect(jsonPath("$[0].organizationRef").value(organizationRef.toString()))
                .andExpect(jsonPath("$[0].phoneNumbers[0].id").value(phoneNumberId.toString()))
                .andExpect(jsonPath("$[0].phoneNumbers[0].type").value("SELF"))
                .andExpect(jsonPath("$[0].phoneNumbers[0].value").value("12345678"));
    }

    @Test
    void shouldFindPersonById() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        personDirectory.findByIdResult = new PersonResponse(
                personId,
                "Jane",
                "Doe",
                "jane@doe.com",
                organizationRef,
                List.of()
        );

        mockMvc.perform(get("/internal/persons/{id}", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@doe.com"))
                .andExpect(jsonPath("$.organizationRef").value(organizationRef.toString()));
    }

    @Test
    void shouldSearchByName() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        personDirectory.searchByNameResult = List.of(
                new PersonResponse(
                        personId,
                        "John",
                        "Doe",
                        "john@doe.com",
                        organizationRef,
                        List.of()
                )
        );

        mockMvc.perform(get("/internal/persons/search").param("name", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(personId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        assertEquals("john", personDirectory.receivedSearchName);
    }

    @Test
    void shouldUpdatePerson() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();

        personDirectory.updateResult = new PersonResponse(
                personId,
                "Jane",
                "Smith",
                "jane@doe.com",
                organizationRef,
                List.of()
        );

        UpdatePersonCmd cmd = new UpdatePersonCmd("Jane", "Smith", "jane@doe.com");

        mockMvc.perform(patch("/internal/persons/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane@doe.com"));

        assertEquals(personId, personDirectory.receivedUpdatePersonId);
        assertEquals("Jane", personDirectory.receivedUpdateCmd.firstName());
        assertEquals("Smith", personDirectory.receivedUpdateCmd.lastName());
        assertEquals("jane@doe.com", personDirectory.receivedUpdateCmd.email());
    }

    @Test
    void shouldAddPhoneNumber() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID phoneNumberId = UUID.randomUUID();

        personDirectory.addPhoneNumberResult = new PhoneNumberResponse(
                phoneNumberId,
                PhoneUserType.SELF,
                "12345678"
        );

        AddPhoneNumberCmd cmd = new AddPhoneNumberCmd(personId, PhoneUserType.SELF, "12345678");

        mockMvc.perform(post("/internal/persons/phone-number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(phoneNumberId.toString()))
                .andExpect(jsonPath("$.type").value("SELF"))
                .andExpect(jsonPath("$.value").value("12345678"));

        assertEquals(personId, personDirectory.receivedAddPhoneNumberCmd.personId());
        assertEquals(PhoneUserType.SELF, personDirectory.receivedAddPhoneNumberCmd.type());
        assertEquals("12345678", personDirectory.receivedAddPhoneNumberCmd.value());
    }

    @Test
    void shouldRemovePhoneNumber() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID phoneNumberId = UUID.randomUUID();

        RemovePhoneNumberCmd cmd = new RemovePhoneNumberCmd(personId, phoneNumberId);

        mockMvc.perform(delete("/internal/persons/phone-number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNoContent());

        assertNotNull(personDirectory.receivedRemovePhoneNumberCmd);
        assertEquals(personId, personDirectory.receivedRemovePhoneNumberCmd.personId());
        assertEquals(phoneNumberId, personDirectory.receivedRemovePhoneNumberCmd.phoneNumberId());
    }

    private static final class StubPersonDirectory implements PersonDirectory {
        private UUID createResult = UUID.randomUUID();
        private PersonResponse findByIdResult;
        private List<PersonResponse> findAllResult = List.of();
        private List<PersonResponse> searchByNameResult = List.of();
        private PersonResponse updateResult = new PersonResponse(
                UUID.randomUUID(),
                "John",
                "Doe",
                "john@doe.com",
                UUID.randomUUID(),
                List.of()
        );
        private PhoneNumberResponse addPhoneNumberResult = new PhoneNumberResponse(
                UUID.randomUUID(),
                PhoneUserType.SELF,
                "12345678"
        );

        private CreatePersonCmd receivedCreateCmd;
        private String receivedSearchName;
        private UUID receivedUpdatePersonId;
        private UpdatePersonCmd receivedUpdateCmd;
        private AddPhoneNumberCmd receivedAddPhoneNumberCmd;
        private RemovePhoneNumberCmd receivedRemovePhoneNumberCmd;

        @Override
        public UUID create(CreatePersonCmd cmd) {
            this.receivedCreateCmd = cmd;
            return createResult;
        }

        @Override
        public Optional<PersonResponse> findById(UUID id) {
            return Optional.ofNullable(findByIdResult);
        }

        @Override
        public List<PersonResponse> findAll() {
            return findAllResult;
        }

        @Override
        public List<PersonResponse> searchByName(String name) {
            this.receivedSearchName = name;
            return searchByNameResult;
        }

        @Override
        public PersonResponse update(UUID id, UpdatePersonCmd cmd) {
            this.receivedUpdatePersonId = id;
            this.receivedUpdateCmd = cmd;
            return updateResult;
        }

        @Override
        public PhoneNumberResponse addPhoneNumber(AddPhoneNumberCmd cmd) {
            this.receivedAddPhoneNumberCmd = cmd;
            return addPhoneNumberResult;
        }

        @Override
        public void removePhoneNumber(RemovePhoneNumberCmd cmd) {
            this.receivedRemovePhoneNumberCmd = cmd;
        }

        @Override
        public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
                throw new UnsupportedOperationException("Unimplemented method 'compensate'");
        }
    }
}