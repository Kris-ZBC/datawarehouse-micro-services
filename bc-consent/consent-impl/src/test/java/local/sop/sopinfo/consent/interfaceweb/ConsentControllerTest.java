package local.sop.sopinfo.consent.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import local.sop.sopinfo.consent.application.api.ConsentDirectory;
import local.sop.sopinfo.consent.application.api.dto.*;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@TestPropertySource(properties = {
                "security.enabled=false"
})
@WebMvcTest(ConsentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ EndpointExceptionHandler.class })
class ConsentControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ConsentDirectory consentDirectory;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        @Test
        void testCreateConsentStatement_ShouldReturnCreatedResponse() throws Exception {
                // Given
                CreateConsentStatementCmd cmd = new CreateConsentStatementCmd(true, "Test statement");
                ConsentStatementResponse expectedResponse = new ConsentStatementResponse(
                                UUID.randomUUID(),
                                "Test statement",
                                true);

                when(consentDirectory.createConsentStatement(cmd))
                                .thenReturn(expectedResponse);

                // When & Then
                mockMvc.perform(post("/internal/consents/statements")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.consentStatementId")
                                                .value(expectedResponse.consentStatementId().toString()))
                                .andExpect(jsonPath("$.statementText").value("Test statement"))
                                .andExpect(jsonPath("$.active").value(true))
                                .andExpect(header().string("Location", "/internal/consents/statements"
                                                + expectedResponse.consentStatementId()));
        }

        @Test
        void testCreateConsentStatement_WithInvalidData_ShouldReturnBadRequest() throws Exception {
                // Given - invalid command (empty statementText)
                CreateConsentStatementCmd cmd = new CreateConsentStatementCmd(true, "");
                // When & Then
                mockMvc.perform(post("/internal/consents/statements")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGrantConsent_ShouldReturnOkResponse() throws Exception {
                // Given
                GrantConsentCmd cmd = new GrantConsentCmd(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                ConsentPurpose.MARKETING,
                                ConsentType.ONE_TIME,
                                ConsentStatus.ACTIVE);
                ConsentResponse expectedResponse = new ConsentResponse(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "ACTIVE",
                                UUID.randomUUID(),
                                "Test statement",
                                "MARKETING",
                                "ONE_TIME",
                                true);

                when(consentDirectory.grantConsent(cmd))
                                .thenReturn(expectedResponse);

                // When & Then
                mockMvc.perform(post("/internal/consents/consent/grant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.consentId").value(expectedResponse.consentId().toString()))
                                .andExpect(jsonPath("$.personReference")
                                                .value(expectedResponse.personReference().toString()))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.consentStatementId")
                                                .value(expectedResponse.consentStatementId().toString()))
                                .andExpect(jsonPath("$.consentStatementText").value("Test statement"))
                                .andExpect(jsonPath("$.consentPurpose").value("MARKETING"))
                                .andExpect(jsonPath("$.consentType").value("ONE_TIME"))
                                .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        void testGrantConsent_WithNonExistentStatement_ShouldReturnNotFound() throws Exception {
                // Given
                GrantConsentCmd cmd = new GrantConsentCmd(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                ConsentPurpose.MARKETING,
                                ConsentType.ONE_TIME,
                                ConsentStatus.ACTIVE);

                when(consentDirectory.grantConsent(cmd))
                                .thenThrow(new NotFoundException("consentstatement.notfound", Map.of()));

                // When & Then
                mockMvc.perform(post("/internal/consents/consent/grant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isNotFound())
                                .andExpect(content().contentType("application/problem+json"))
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.title").value("Not Found"))
                                .andExpect(jsonPath("$.detail").value("consentstatement.notfound"))
                                .andExpect(jsonPath("$.key").value("consentstatement.notfound"));
        }

        @Test
        void testGetConsent_ShouldReturnOkResponse() throws Exception {
                // Given
                UUID consentId = UUID.randomUUID();
                ConsentResponse expectedResponse = new ConsentResponse(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "ACTIVE",
                                UUID.randomUUID(),
                                "Test statement",
                                "MARKETING",
                                "ONE_TIME",
                                true);

                when(consentDirectory.getConsent(consentId))
                                .thenReturn(Optional.of(expectedResponse));

                // When & Then
                mockMvc.perform(get("/internal/consents/consent/{id}", consentId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.consentId").value(expectedResponse.consentId().toString()))
                                .andExpect(jsonPath("$.personReference")
                                                .value(expectedResponse.personReference().toString()))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.consentStatementId")
                                                .value(expectedResponse.consentStatementId().toString()))
                                .andExpect(jsonPath("$.consentStatementText").value("Test statement"))
                                .andExpect(jsonPath("$.consentPurpose").value("MARKETING"))
                                .andExpect(jsonPath("$.consentType").value("ONE_TIME"))
                                .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        void testGetConsent_WithNonExistentConsent_ShouldReturnNoContent() throws Exception {
                // Given
                UUID consentId = UUID.randomUUID();
                when(consentDirectory.getConsent(consentId))
                                .thenReturn(Optional.empty());

                // When & Then
                mockMvc.perform(get("/internal/consents/consent/{id}", consentId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));
        }

        @Test
        void testWithdrawConsent_ShouldReturnOkResponse() throws Exception {
                // Given
                RevokeConsentCmd cmd = new RevokeConsentCmd(UUID.randomUUID());
                ConsentResponse expectedResponse = new ConsentResponse(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "WITHDRAWN",
                                UUID.randomUUID(),
                                "Test statement",
                                "MARKETING",
                                "ONE_TIME",
                                false);

                when(consentDirectory.withdrawConsent(cmd))
                                .thenReturn(expectedResponse);

                // When & Then
                mockMvc.perform(post("/internal/consents/consent/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.consentId").value(expectedResponse.consentId().toString()))
                                .andExpect(jsonPath("$.personReference")
                                                .value(expectedResponse.personReference().toString()))
                                .andExpect(jsonPath("$.status").value("WITHDRAWN"))
                                .andExpect(jsonPath("$.consentStatementId")
                                                .value(expectedResponse.consentStatementId().toString()))
                                .andExpect(jsonPath("$.consentStatementText").value("Test statement"))
                                .andExpect(jsonPath("$.consentPurpose").value("MARKETING"))
                                .andExpect(jsonPath("$.consentType").value("ONE_TIME"))
                                .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        void testUpdateConsentStatement_ShouldReturnOkResponse() throws Exception {
                // Given
                UpdateConsentStatementCmd cmd = new UpdateConsentStatementCmd(
                                UUID.randomUUID(),
                                "Updated statement text",
                                null,
                                null);
                ConsentStatementResponse expectedResponse = new ConsentStatementResponse(
                                UUID.randomUUID(),
                                "Updated statement text",
                                false);

                when(consentDirectory.updateConsentStatement(cmd))
                                .thenReturn(expectedResponse);

                // When & Then
                mockMvc.perform(put("/internal/consents/statements/statement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.consentStatementId")
                                                .value(expectedResponse.consentStatementId().toString()))
                                .andExpect(jsonPath("$.statementText").value("Updated statement text"))
                                .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        void testGetConsentStatement_ShouldReturnOkResponse() throws Exception {
                // Given
                UUID id = UUID.randomUUID();
                ConsentStatementResponse expectedResponse = new ConsentStatementResponse(
                                UUID.randomUUID(),
                                "Test statement",
                                true);

                when(consentDirectory.getConsentStatement(any(FetchConsentStatementQuery.class)))
                                .thenReturn(Optional.of(expectedResponse));

                // When & Then
                mockMvc.perform(get("/internal/consents/statements/statement")
                                .param("id", id.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.consentStatementId")
                                                .value(expectedResponse.consentStatementId().toString()))
                                .andExpect(jsonPath("$.statementText").value("Test statement"))
                                .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        void testGetConsentStatement_WithNonExistentStatement_ShouldReturnNoContent() throws Exception {
                // Given
                UUID id = UUID.randomUUID();

                when(consentDirectory.getConsentStatement(any(FetchConsentStatementQuery.class)))
                                .thenReturn(Optional.empty());

                // When & Then
                mockMvc.perform(get("/internal/consents/statements/statement")
                                .param("id", id.toString()))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));
        }

        @Test
        void testGetAllConsentStatements_ShouldReturnListOfStatements() throws Exception {
                // Given
                ConsentStatementResponse response1 = new ConsentStatementResponse(
                                UUID.randomUUID(),
                                "Statement 1",
                                true);
                ConsentStatementResponse response2 = new ConsentStatementResponse(
                                UUID.randomUUID(),
                                "Statement 2",
                                false);

                when(consentDirectory.getAllConsentStatements())
                                .thenReturn(List.of(response1, response2));

                // When & Then
                mockMvc.perform(get("/internal/consents/statements"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].statementText").value("Statement 1"))
                                .andExpect(jsonPath("$[0].active").value(true))
                                .andExpect(jsonPath("$[1].statementText").value("Statement 2"))
                                .andExpect(jsonPath("$[1].active").value(false));

                verify(consentDirectory).getAllConsentStatements();
        }

        @Test
        void compensate_WhenServiceReturnsResult_ShouldReturn200() throws Exception {
                // Given
                UUID statementId = UUID.randomUUID();
                CompensateConsentStatementCmd cmd = new CompensateConsentStatementCmd(
                                String.class,
                                SagaOutcome.COMPENSATE);
                ResponseCompensated expectedResult = new ResponseCompensated(
                                SagaOutcome.COMPENSATED,
                                true);

                when(consentDirectory.compensate(statementId, cmd.clazz(), cmd.sagaState()))
                                .thenReturn(expectedResult);

                // When & Then
                mockMvc.perform(post("/internal/consents/statements/{id}/compensate/create", statementId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.sagaState").value("COMPENSATED"))
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void compensate_WhenServiceReturnsNull_ShouldReturn204() throws Exception {
                // Given
                UUID statementId = UUID.randomUUID();
                CompensateConsentStatementCmd cmd = new CompensateConsentStatementCmd(
                                String.class,
                                SagaOutcome.COMPENSATE);

                when(consentDirectory.compensate(statementId, cmd.clazz(), cmd.sagaState()))
                                .thenReturn(null);

                // When & Then
                mockMvc.perform(post("/internal/consents/statements/{id}/compensate/create", statementId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cmd)))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));
        }

        @Test
        void getConsentForPersonAndPurpose_WhenConsentExists_ShouldReturnOk() throws Exception {
                // Given
                UUID personId = UUID.randomUUID();

                ConsentResponse response = new ConsentResponse(
                                UUID.randomUUID(),
                                personId,
                                "ACTIVE",
                                UUID.randomUUID(),
                                "Test statement",
                                "MARKETING",
                                "ONE_TIME",
                                true);

                when(consentDirectory.getConsentForPersonAndPurpose(any()))
                                .thenReturn(Optional.of(response));

                // When & Then
                mockMvc.perform(get("/internal/consents/consent")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                    {
                                                        "personId": "%s",
                                                        "purpose": "MARKETING"
                                                    }
                                                """.formatted(personId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.consentPurpose").value("MARKETING"));
        }

        @Test
        void getConsentForPersonAndPurpose_WhenConsentDoesNotExist_ShouldReturnNoContent() throws Exception {
                // Given
                UUID personId = UUID.randomUUID();

                when(consentDirectory.getConsentForPersonAndPurpose(any()))
                                .thenReturn(Optional.empty());

                // When & Then
                mockMvc.perform(get("/internal/consents/consent")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                    {
                                                        "personId": "%s",
                                                        "purpose": "MARKETING"
                                                    }
                                                """.formatted(personId)))
                                .andExpect(status().isNoContent());
        }

        @Test
        void compensateConsent_Create_WhenSuccess_ShouldReturnOk() throws Exception {
                // Given
                UUID id = UUID.randomUUID();

                PayloadCompensateCreate payload = new PayloadCompensateCreate(
                                String.class,
                                SagaOutcome.COMPENSATE);

                ResponseCompensated response = new ResponseCompensated(
                                SagaOutcome.COMPENSATED,
                                true);

                when(consentDirectory.compensateConsent(any(), any(), any()))
                                .thenReturn(response);

                // When & Then
                mockMvc.perform(put("/internal/consents/consent/{id}/compensate/create", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sagaState").value("COMPENSATED"))
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void compensateConsent_Create_WhenNull_ShouldReturnNoContent() throws Exception {
                // Given
                UUID id = UUID.randomUUID();

                PayloadCompensateCreate payload = new PayloadCompensateCreate(
                                String.class,
                                SagaOutcome.COMPENSATE);

                when(consentDirectory.compensateConsent(any(), any(), any()))
                                .thenReturn(null);

                // When & Then
                mockMvc.perform(put("/internal/consents/consent/{id}/compensate/create", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isNoContent());
        }

        @Test
        void compensateConsent_Update_WhenSuccess_ShouldReturnOk() throws Exception {
                // Given
                UUID id = UUID.randomUUID();

                PayloadCompensateCreate payload = new PayloadCompensateCreate(
                                String.class,
                                SagaOutcome.COMPENSATE);

                ResponseCompensated response = new ResponseCompensated(
                                SagaOutcome.COMPENSATED,
                                true);

                when(consentDirectory.compensateConsentWithdrawalUpdate(any(), any(), any()))
                                .thenReturn(response);

                // When & Then
                mockMvc.perform(post("/internal/consents/consent/{id}/compensate/update", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sagaState").value("COMPENSATED"))
                                .andExpect(jsonPath("$.success").value(true));
        }

}