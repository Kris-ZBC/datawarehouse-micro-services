package local.sop.sopinfo.consent.saga.application.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
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
 
import local.sop.sopinfo.consent.saga.application.api.ConsentSagaDirectory;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.GrantConsentCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.RevokeConsentCmd;
import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
 
@WebMvcTest(ConsentSagaController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConsentSagaControllerTest {
 
    private static final String URL = "/internal/saga/consents/statements";
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private ObjectMapper objectMapper;
 
    @MockitoBean
    private ConsentSagaDirectory consentDirectory;
    private UUID consentId;
    private UUID consentStatementId;
    private CreateConsentStatementCmd validCmd;
    private ConsentStatementResponse consentStatementResponse;
    private GrantConsentCmd validGrantCmd;
    private RevokeConsentCmd validRevokeCmd;
 
    @BeforeEach
    void setUp() {
        consentStatementId = UUID.randomUUID();
        consentId = UUID.randomUUID();

 
        validCmd = new CreateConsentStatementCmd(
        UUID.randomUUID(), // sessionId
        true,
        "This is a valid consent statement text",
        UUID.randomUUID(),
        ActorType.USER,
        Severity.INFO,
        "originSystem",
        "originService",
        "originComponent",
        "someData",
        "someDescription"
        );
        validGrantCmd = new GrantConsentCmd(
        UUID.randomUUID(), // sessionId
        UUID.randomUUID(), // personRef
        UUID.randomUUID(), // consentStatementRef
        ConsentPurpose.MARKETING,
        ConsentType.REQUIRED,
        ConsentStatus.ACTIVE,
        UUID.randomUUID(),
        ActorType.USER,
        Severity.INFO,
        "originSystem",
        "originService",
        "originComponent",
        "someData",
        "someDescription"
        );

        validRevokeCmd = new RevokeConsentCmd(
        UUID.randomUUID(), // sessionId
        UUID.randomUUID(), // personRef
        UUID.randomUUID(), // consentStatementRef
        ActorType.USER,
        Severity.INFO,
        "originSystem",
        "originService",
        "originComponent",
        "someData",
        "someDescription"
        );

 
        consentStatementResponse = new ConsentStatementResponse(
        consentStatementId,
        validCmd.statementText(),
        validCmd.active()
        );
    }
 
    /* =========================================================== *
     *  Happy path
     * =========================================================== */
 
    @Nested
    class HappyPath {
 
        @BeforeEach
        void directoryReturnsOk() {
            when(consentDirectory.createConsentStatement(any())).thenReturn(consentStatementResponse);
            when(consentDirectory.grant(any())).thenReturn(new ConsentResponse(
                    consentId,
                    UUID.randomUUID(),
                    "ACTIVE",
                    consentStatementId,
                    consentStatementResponse.statementText(),
                    "MARKETING",
                    "REQUIRED",
                    true
            ));
            when(consentDirectory.withdraw(any())).thenReturn(new ConsentResponse(
                    consentId,
                    UUID.randomUUID(),
                    "WITHDRAWN",
                    consentStatementId,
                    consentStatementResponse.statementText(),
                    "MARKETING",
                    "REQUIRED",
                    false
            ));
        }
 
        @Test
        void create_shouldReturn201_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(status().isCreated());
        }
 
        @Test
        void create_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(header().string("Location",
                            "/internal/saga/consents/statements/" + consentStatementId));
        }
 
        @Test
        void create_shouldReturnConsentStatementId_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(jsonPath("$.consentStatementId").value(consentStatementId.toString()));
        }
 
        @Test
        void create_shouldReturnStatementText_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(jsonPath("$.statementText").value(validCmd.statementText()));
        }
 
        @Test
        void create_shouldReturnActive_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(jsonPath("$.active").value(validCmd.active()));
        }
 
        @Test
        void create_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
 
        @Test
        void create_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCmd)));
 
            verify(consentDirectory).createConsentStatement(any(CreateConsentStatementCmd.class));
        }

        @Test
        void grant_shouldReturn201_whenRequestIsValid() throws Exception {
                mockMvc.perform(post("/internal/saga/consents/consent/grant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validGrantCmd)))
                                .andExpect(status().isCreated());
        }

        @Test
        void grant_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/grant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validGrantCmd)))
                                .andExpect(header().string("Location",
                                                "/internal/saga/consents/consent/grant/" + consentId));
        }

        @Test
        void grant_shouldReturnResponseBody_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/grant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validGrantCmd)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.consentId")
                                                .value(consentId.toString()));
        }

        @Test
        void grant_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/grant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validGrantCmd)));

                verify(consentDirectory).grant(any(GrantConsentCmd.class));
        }

        @Test
        void withdraw_shouldReturn200_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRevokeCmd)))
                                .andExpect(status().isOk());
        }

        @Test
        void withdraw_shouldReturnResponseBody_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRevokeCmd)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.consentId")
                                                .value(consentId.toString()));
        }

        @Test
        void withdraw_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRevokeCmd)))
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void withdraw_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {

                mockMvc.perform(post("/internal/saga/consents/consent/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRevokeCmd)));

                verify(consentDirectory).withdraw(any(RevokeConsentCmd.class));
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
 
        @Test
        void create_shouldReturn400_whenActiveIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withActive(null))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
 
        @Test
        void create_shouldReturn400_whenStatementTextIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withStatementText(null))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenStatementTextIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withStatementText("   "))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenStatementTextExceeds1000Characters() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withStatementText("x".repeat(1001)))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn201_whenStatementTextIsExactly1000Characters() throws Exception {
            when(consentDirectory.createConsentStatement(any())).thenReturn(consentStatementResponse);
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withStatementText("x".repeat(1000)))))
                    .andExpect(status().isCreated());
        }
 
        @Test
        void create_shouldReturn400_whenActorRefIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withActorRef(null))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenActorTypeIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withActorType(null))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenSeverityIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withSeverity(null))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenOriginSystemIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withOriginSystem("   "))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenOriginServiceIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withOriginService("   "))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenOriginComponentIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withOriginComponent("   "))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenDataIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withData("   "))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturn400_whenDescriptionIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withDescription("   "))))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        void create_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withActive(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }
 
        @Test
        void create_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withActive(null))));
 
            verify(consentDirectory, never()).createConsentStatement(any());
        }
    }
 
    /* =========================================================== *
     *  Service / domain layer failures — RFC 7807 ProblemDetail
     * =========================================================== */
 
    @Nested
    class ServiceFailures {
 
        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(consentDirectory.createConsentStatement(any()))
                    .thenThrow(new ConflictException("consentstatement.notcreated",
                            Map.of("object", "consentStatement")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(status().isConflict());
        }
 
        @Test
        void create_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(consentDirectory.createConsentStatement(any()))
                    .thenThrow(new ConflictException("consentstatement.notcreated",
                            Map.of("object", "consentStatement")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }
 
        @Test
        void create_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(consentDirectory.createConsentStatement(any()))
                    .thenThrow(new ConflictException("consentstatement.notcreated",
                            Map.of("object", "consentStatement")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(jsonPath("$.key").value("consentstatement.notcreated"));
        }
 
        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(consentDirectory.createConsentStatement(any()))
                    .thenThrow(new ConflictException("auditlog.notcreated",
                            Map.of("object", "auditlog")));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notcreated"));
        }
 
        @Test
        void create_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(consentDirectory.createConsentStatement(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(status().isInternalServerError());
        }
 
        @Test
        void create_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(consentDirectory.createConsentStatement(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }
    }
 
 
    /* =========================================================== *
     *  Builder helpers — produce clean variants of validCmd
     * =========================================================== */
 
    private CreateConsentStatementCmd withActive(Boolean active) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), active, validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withStatementText(String text) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), text,
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withActorRef(UUID actorRef) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                actorRef, validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withActorType(ActorType actorType) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), actorType, validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withSeverity(Severity severity) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), severity,
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withOriginSystem(String originSystem) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                originSystem, validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withOriginService(String originService) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), originService, validCmd.originComponent(),
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withOriginComponent(String originComponent) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), originComponent,
                validCmd.data(), validCmd.description());
    }
 
    private CreateConsentStatementCmd withData(String data) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                data, validCmd.description());
    }
 
    private CreateConsentStatementCmd withDescription(String description) {
        return new CreateConsentStatementCmd(validCmd.sessionId(), validCmd.active(), validCmd.statementText(),
                validCmd.actorRef(), validCmd.actorType(), validCmd.severity(),
                validCmd.originSystem(), validCmd.originService(), validCmd.originComponent(),
                validCmd.data(), description);
    }
}