package local.sop.datawarehouse.message.saga.application.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
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
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.datawarehouse.message.saga.application.api.MessageSagaDirectory;
import local.sop.datawarehouse.message.saga.application.api.dto.CreateMessageCmd;
import local.sop.datawarehouse.message.saga.application.api.dto.MessageResponse;

@WebMvcTest(MessageSagaController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MessageSagaControllerTest {

    private static final String URL = "/internal/saga/messages/create";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageSagaDirectory messageSagaDirectory;

    private UUID messageId;
    private CreateMessageCmd createCmd;
    private MessageResponse response;

    @BeforeEach
    void setUp() {
        messageId = UUID.randomUUID();

        createCmd = new CreateMessageCmd(
            UUID.randomUUID(), // sessionId
            UUID.randomUUID(), // senderPersonRef
            UUID.randomUUID(), // educationRef
            "test message",    // message
            UUID.randomUUID(), // actorRef
            ActorType.USER,
            Severity.INFO,
            "system",
            "service",
            "component",
            "data",
            "description"
        );

        response = new MessageResponse(
            messageId,
            OffsetDateTime.now(),
            "test message",
            createCmd.senderPersonRef()
        );
    }

    /* =========================================================== *
     *  Happy path
     * =========================================================== */

    @Nested
    class HappyPath {

        @BeforeEach
        void directoryReturnsOk() {
            when(messageSagaDirectory.createMessage(any())).thenReturn(response);
        }

        @Test
        void create_shouldReturn201_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(status().isCreated());
        }

        @Test
        void create_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(header().string("Location",
                            "/internal/saga/messages/create/" + messageId));
        }

        @Test
        void create_shouldReturnMessageId_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(jsonPath("$.id").value(messageId.toString()));
        }

        @Test
        void create_shouldReturnMessage_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(jsonPath("$.message").value(createCmd.message()));
        }

        @Test
        void create_shouldReturnSenderPersonRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(jsonPath("$.senderPersonRef").value(createCmd.senderPersonRef().toString()));
        }

        @Test
        void create_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void create_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCmd)));

            verify(messageSagaDirectory).createMessage(any(CreateMessageCmd.class));
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
        void create_shouldReturn400_whenSessionIdIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withSessionId(null))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void create_shouldReturn400_whenSenderPersonRefIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withSenderPersonRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenEducationRefIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withEducationRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenMessageIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withMessage(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenMessageIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withMessage("   "))))
                    .andExpect(status().isBadRequest());
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
                            .content(objectMapper.writeValueAsString(withSessionId(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void create_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withSessionId(null))));

            verify(messageSagaDirectory, never()).createMessage(any());
        }
    }

    /* =========================================================== *
     *  Service / domain layer failures — RFC 7807 ProblemDetail
     * =========================================================== */

    @Nested
    class ServiceFailures {

        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(messageSagaDirectory.createMessage(any()))
                    .thenThrow(new ConflictException("message.not.created",
                            Map.of("object", "message")));

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(status().isConflict());
        }

        @Test
        void create_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(messageSagaDirectory.createMessage(any()))
                    .thenThrow(new ConflictException("message.not.created",
                            Map.of("object", "message")));

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }

        @Test
        void create_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(messageSagaDirectory.createMessage(any()))
                    .thenThrow(new ConflictException("message.not.created",
                            Map.of("object", "message")));

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(jsonPath("$.key").value("message.not.created"));
        }

        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(messageSagaDirectory.createMessage(any()))
                    .thenThrow(new ConflictException("auditlog.not.created",
                            Map.of("object", "auditlog")));

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.not.created"));
        }

        @Test
        void create_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(messageSagaDirectory.createMessage(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void create_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(messageSagaDirectory.createMessage(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }
    }

    /* =========================================================== *
     *  Builder helpers — produce clean variants of createCmd
     * =========================================================== */

    private CreateMessageCmd withSessionId(UUID sessionId) {
        return new CreateMessageCmd(sessionId, createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withSenderPersonRef(UUID senderPersonRef) {
        return new CreateMessageCmd(createCmd.sessionId(), senderPersonRef, createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withEducationRef(UUID educationRef) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), educationRef,
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withMessage(String message) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                message, createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withActorRef(UUID actorRef) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), actorRef, createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withActorType(ActorType actorType) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), actorType, createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withSeverity(Severity severity) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), severity,
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withOriginSystem(String originSystem) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                originSystem, createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withOriginService(String originService) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), originService, createCmd.originComponent(),
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withOriginComponent(String originComponent) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), originComponent,
                createCmd.data(), createCmd.description());
    }

    private CreateMessageCmd withData(String data) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                data, createCmd.description());
    }

    private CreateMessageCmd withDescription(String description) {
        return new CreateMessageCmd(createCmd.sessionId(), createCmd.senderPersonRef(), createCmd.educationRef(),
                createCmd.message(), createCmd.actorRef(), createCmd.actorType(), createCmd.severity(),
                createCmd.originSystem(), createCmd.originService(), createCmd.originComponent(),
                createCmd.data(), description);
    }
}