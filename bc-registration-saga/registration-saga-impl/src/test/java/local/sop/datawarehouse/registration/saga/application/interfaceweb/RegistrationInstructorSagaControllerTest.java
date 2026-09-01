package local.sop.datawarehouse.registration.saga.application.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
 
import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.datawarehouse.registration.saga.application.api.RegistrationDirectory;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreatedInstructorResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;

@WebMvcTest(RegistrationSagaController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RegistrationInstructorSagaControllerTest {
 
    private static final String URL = "/internal/saga/registrations/instructors";
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private ObjectMapper objectMapper;
 
    @MockitoBean
    private RegistrationDirectory registrationDirectory;
 
    private UUID registrationId;
    private CreateInstructorRegistrationCmd instructorCmd;
    private CreatedInstructorResponse registrationResponse;
    
    private List<CreatePhoneNumberCmd> phoneCmd;
    @BeforeEach
    void setUp() {
        
        registrationId = UUID.randomUUID();
        instructorCmd = new CreateInstructorRegistrationCmd(
                "Daniel",
                "S",
                "dani423j@zbc.dk",
                UUID.randomUUID(),
                phoneCmd,
                "dani423j",
                "ACTIVE",
                List.of(new CreateInstructorRegistrationCmd.ConsentStatement(UUID.randomUUID(), ConsentStatus.ACTIVE)));

        registrationResponse = new CreatedInstructorResponse(registrationId);
    }
 
    /* =========================================================== *
     *  Happy path
     * =========================================================== */
 
    @Nested
    class HappyPath {
 
        @BeforeEach
        void directoryReturnsOk() {
            when(registrationDirectory.registerInstructor(any())).thenReturn(registrationResponse);
        }
 
        @Test
        void create_shouldReturn201_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(status().isCreated());
        }
 
        @Test
        void create_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(header().string("Location",
                            "/internal/saga/registrations/instructors/" + registrationId.toString()));
        }
 
        @Test
        void create_shouldReturnId_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(jsonPath("$.id").value(registrationId.toString()));
        }
 
        @Test
        void create_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
 
        @Test
        void create_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(instructorCmd)));
 
            verify(registrationDirectory).registerInstructor(instructorCmd);
        }
    }
 
    /* =========================================================== *
     *  Validation — @Valid on @RequestBody
     * =========================================================== */
 
    @Nested
    class Validation {
 
        @Test
        void create_shouldReturn400_whenBodyIsMissing() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenBodyIsMalformedJson() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not valid json"))
                    .andExpect(status().isBadRequest());
        }
    }
 
    /* =========================================================== *
     *  Service / domain layer failures — RFC 7807 ProblemDetail
     * =========================================================== */
 
    @Nested
    class ServiceFailures {
 
        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(registrationDirectory.registerInstructor(any()))
                    .thenThrow(new ConflictException("consentstatement.notcreated",
                            Map.of("object", "consentStatement")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(status().isConflict());
        }
 
        @Test
        void create_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(registrationDirectory.registerInstructor(any()))
                    .thenThrow(new ConflictException("consentstatement.notcreated",
                            Map.of("object", "consentStatement")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }
 
        @Test
        void create_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(registrationDirectory.registerInstructor(any()))
                    .thenThrow(new ConflictException("consentstatement.notcreated",
                            Map.of("object", "consentStatement")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(jsonPath("$.key").value("consentstatement.notcreated"));
        }
 
        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(registrationDirectory.registerInstructor(any()))
                    .thenThrow(new ConflictException("auditlog.notcreated",
                            Map.of("object", "auditlog")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notcreated"));
        }
 
        @Test
        void create_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(registrationDirectory.registerInstructor(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(status().isInternalServerError());
        }
 
        @Test
        void create_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(registrationDirectory.registerInstructor(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(instructorCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }
    }
}