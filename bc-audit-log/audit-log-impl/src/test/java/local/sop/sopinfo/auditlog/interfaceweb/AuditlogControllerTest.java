package local.sop.sopinfo.auditlog.interfaceweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.sop.sopinfo.auditlog.application.api.AuditlogDirectory;
import local.sop.sopinfo.auditlog.application.api.dto.AuditlogResponse;
import local.sop.sopinfo.auditlog.application.api.dto.CompensateAuditlogCmd;
import local.sop.sopinfo.auditlog.application.api.dto.CreateAuditlogCmd;
import local.sop.sopinfo.auditlog.application.api.dto.CreatedAuditlogResponse;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditlogController.class)
class AuditlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditlogDirectory directory;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAuditlog_shouldReturnCreatedResponse() throws Exception {
        UUID actorRef = UUID.randomUUID();
        UUID expectedId = UUID.randomUUID();

        CreateAuditlogCmd cmd = new CreateAuditlogCmd(
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "system",
                "service",
                "component",
                "{\"key\":\"value\"}",
                "Optional description"
        );

        CreatedAuditlogResponse mockResponse = new CreatedAuditlogResponse(expectedId);
        Mockito.when(directory.createAuditlog(any(CreateAuditlogCmd.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/internal/auditlogs")
                        .with(Objects.requireNonNull(csrf()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(cmd))))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":\"" + expectedId + "\"}"));

        Mockito.verify(directory).createAuditlog(any(CreateAuditlogCmd.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAuditlog_shouldFailValidation_whenMissingRequiredFields() throws Exception {
        String invalidJson = """
                {
                  "actorRef": "%s",
                  "actorType": "USER",
                  "severity": "INFO",
                  "originService": "service",
                  "originComponent": "component",
                  "data": "{\\"key\\":\\"value\\"}"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/internal/auditlogs")
                        .with(Objects.requireNonNull(csrf()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(invalidJson)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(directory);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAuditlog_emptyRequest_shouldFailValidation() throws Exception {
        mockMvc.perform(post("/internal/auditlogs")
                        .with(Objects.requireNonNull(csrf()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{}"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(directory);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void findAll_shouldReturnOkAndResponseBody() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();

        List<AuditlogResponse> responses = List.of(
                new AuditlogResponse(
                        id,
                        actorRef,
                        ActorType.USER,
                        Severity.INFO,
                        "audit-system",
                        "audit-service",
                        "audit-component",
                        "{\"a\":1}",
                        "desc",
                        Instant.parse("2026-03-18T12:00:00Z")
                )
        );

        Mockito.when(directory.findAll()).thenReturn(responses);

        mockMvc.perform(get("/internal/auditlogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].actorRef").value(actorRef.toString()))
                .andExpect(jsonPath("$[0].actorType").value("USER"))
                .andExpect(jsonPath("$[0].severity").value("INFO"))
                .andExpect(jsonPath("$[0].originSystem").value("audit-system"))
                .andExpect(jsonPath("$[0].originService").value("audit-service"))
                .andExpect(jsonPath("$[0].originComponent").value("audit-component"))
                .andExpect(jsonPath("$[0].data").value("{\"a\":1}"))
                .andExpect(jsonPath("$[0].description").value("desc"))
                .andExpect(jsonPath("$[0].timestamp").value("2026-03-18T12:00:00Z"));

        Mockito.verify(directory).findAll();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void search_shouldForwardAllParams_andReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();

        List<AuditlogResponse> responses = List.of(
                new AuditlogResponse(
                        id,
                        actorRef,
                        ActorType.USER,
                        Severity.WARNING,
                        "search-system",
                        "search-service",
                        "search-component",
                        "{\"matched\":true}",
                        "matched",
                        Instant.parse("2026-03-18T13:00:00Z")
                )
        );

        Mockito.when(directory.findBySearchParams(
                eq(id),
                eq(actorRef),
                eq(ActorType.USER),
                eq(Severity.WARNING),
                eq("search-system"),
                eq("search-service"),
                eq("search-component")
        )).thenReturn(responses);

        mockMvc.perform(get("/internal/auditlogs/search")
                        .param("id", id.toString())
                        .param("actorRef", actorRef.toString())
                        .param("actorType", "USER")
                        .param("severity", "WARNING")
                        .param("originSystem", "search-system")
                        .param("originService", "search-service")
                        .param("originComponent", "search-component"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].actorRef").value(actorRef.toString()))
                .andExpect(jsonPath("$[0].actorType").value("USER"))
                .andExpect(jsonPath("$[0].severity").value("WARNING"))
                .andExpect(jsonPath("$[0].originSystem").value("search-system"))
                .andExpect(jsonPath("$[0].originService").value("search-service"))
                .andExpect(jsonPath("$[0].originComponent").value("search-component"))
                .andExpect(jsonPath("$[0].data").value("{\"matched\":true}"))
                .andExpect(jsonPath("$[0].description").value("matched"))
                .andExpect(jsonPath("$[0].timestamp").value("2026-03-18T13:00:00Z"));

        Mockito.verify(directory).findBySearchParams(
                eq(id),
                eq(actorRef),
                eq(ActorType.USER),
                eq(Severity.WARNING),
                eq("search-system"),
                eq("search-service"),
                eq("search-component")
        );
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void search_shouldAllowMissingParams() throws Exception {
        Mockito.when(directory.findBySearchParams(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(List.of());

        mockMvc.perform(get("/internal/auditlogs/search"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(directory).findBySearchParams(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }
    @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        void getById_shouldReturnOk_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();

        AuditlogResponse response = new AuditlogResponse(
                id,
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "sys",
                "svc",
                "cmp",
                "{\"x\":1}",
                "desc",
                Instant.parse("2026-03-18T12:00:00Z")
        );

        Mockito.when(directory.findById(id)).thenReturn(java.util.Optional.of(response));

        mockMvc.perform(get("/internal/auditlogs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.actorRef").value(actorRef.toString()))
                .andExpect(jsonPath("$.actorType").value("USER"))
                .andExpect(jsonPath("$.severity").value("INFO"))
                .andExpect(jsonPath("$.originSystem").value("sys"))
                .andExpect(jsonPath("$.originService").value("svc"))
                .andExpect(jsonPath("$.originComponent").value("cmp"))
                .andExpect(jsonPath("$.data").value("{\"x\":1}"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.timestamp").value("2026-03-18T12:00:00Z"));

        Mockito.verify(directory).findById(id);
   }

  @Test
@WithMockUser(username = "testuser", roles = {"USER"})
void compensate_shouldReturnOkResponse_whenResultIsNotNull() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.COMPENSATE);
    ResponseCompensated mockResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

    when(directory.compensate(any(UUID.class), any(Class.class), any(SagaOutcome.class)))
            .thenReturn(mockResult);

    // When & Then
    mockMvc.perform(put("/internal/auditlogs/{id}/compensate/create", id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cmd)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(mockResult)));

    verify(directory).compensate(any(UUID.class), any(Class.class), any(SagaOutcome.class));
}

@Test
@WithMockUser(username = "testuser", roles = {"USER"})
void compensate_shouldReturnNoContentResponse_whenResultIsNull() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.COMPENSATE);

    when(directory.compensate(any(UUID.class), any(Class.class), any(SagaOutcome.class)))
            .thenReturn(null);

    // When & Then
    mockMvc.perform(put("/internal/auditlogs/{id}/compensate/create", id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cmd)))
            .andExpect(status().isNoContent());

    verify(directory).compensate(any(UUID.class), any(Class.class), any(SagaOutcome.class));
}

@Test
@WithMockUser(username = "testuser", roles = {"USER"})
void compensate_shouldReturnBadRequest_whenRequestBodyIsInvalid() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    String invalidJson = "{\"sagaState\":\"INVALID_STATE\",\"clazz\":\"\"}";

    // When & Then
    mockMvc.perform(put("/internal/auditlogs/{id}/compensate/create", id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
            .andExpect(status().isBadRequest());

    verify(directory, never()).compensate(any(UUID.class), any(Class.class), any(SagaOutcome.class));
}

@Test
void compensate_shouldReturnUnauthorized_whenNoAuthentication() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.COMPENSATE);

    // When & Then
    mockMvc.perform(put("/internal/auditlogs/{id}/compensate/create", id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cmd)))
            .andExpect(status().isUnauthorized());

    verify(directory, never()).compensate(any(UUID.class), any(Class.class), any(SagaOutcome.class));
}
}