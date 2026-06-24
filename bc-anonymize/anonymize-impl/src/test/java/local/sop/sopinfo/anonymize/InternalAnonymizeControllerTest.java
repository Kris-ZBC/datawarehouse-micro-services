package local.sop.sopinfo.anonymize;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import local.sop.sopinfo.anonymize.application.api.AnonymizeDirectory;
import local.sop.sopinfo.anonymize.application.api.dto.AnonymizeResponse;
import local.sop.sopinfo.anonymize.application.api.dto.CreateAnonymizeCmd;
import local.sop.sopinfo.anonymize.application.api.dto.FetchByIdQuery;
import local.sop.sopinfo.anonymize.application.api.dto.FetchByParamsQuery;
import local.sop.sopinfo.anonymize.interfaceweb.InternalAnonymizeController;
import local.sop.sopinfo.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;


@TestPropertySource(properties = {
    "security.enabled=false"
})
@WebMvcTest(InternalAnonymizeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({EndpointExceptionHandler.class})
public class InternalAnonymizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnonymizeDirectory anonymizeDirectory;

    private ObjectMapper objectMapper;
    private UUID testId;
    private UUID testPersonRef;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        testId = UUID.randomUUID();
        testPersonRef = UUID.randomUUID();
    }

    // ===== CREATE TESTS =====

    @Test
    @DisplayName("Should create anonymize successfully")
    void testCreate_ShouldCreateAnonymizeSuccessfully() throws Exception {
        // Given
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(testPersonRef);
        AnonymizeResponse expectedResponse = new AnonymizeResponse(testId, testPersonRef);

        when(anonymizeDirectory.create(cmd))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.anonymizationId").value(testId.toString()))
                .andExpect(jsonPath("$.personRef").value(testPersonRef.toString()))
                .andExpect(header().string("Location", "/internal/anonymizations" + testId));
    }

    @Test
    @DisplayName("Should return validation error for invalid create command")
    void testCreate_WithInvalidCommand_ShouldReturnBadRequest() throws Exception {
        // Given - invalid command (null personRef)
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(null);

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    @DisplayName("Should handle domain exception during create")
    void testCreate_WhenDomainThrowsException_ShouldReturnError() throws Exception {
        // Given
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(testPersonRef);
        when(anonymizeDirectory.create(cmd))
            .thenThrow(new ValidationException("Invalid person reference", Map.of("key", "value")));

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
    }

    // ===== FETCH BY ID TESTS =====

    @Test
    @DisplayName("Should fetch anonymize by ID successfully")
    void testFetchById_ShouldFetchSuccessfully() throws Exception {
        // Given
        FetchByIdQuery query = new FetchByIdQuery(testId);
        AnonymizeResponse expectedResponse = new AnonymizeResponse(testId, testPersonRef);

        when(anonymizeDirectory.findById(query))
            .thenReturn(Optional.of(expectedResponse));

        // When & Then
        mockMvc.perform(get("/internal/anonymizations/anonymize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.anonymizationId").value(testId.toString()))
                .andExpect(jsonPath("$.personRef").value(testPersonRef.toString()));
    }


    @Test
    @DisplayName("Should return no content when anonymize not found")
    void testFetchById_WhenNotFound_ShouldReturnNoContent() throws Exception {
        // Given
        FetchByIdQuery query = new FetchByIdQuery(testId);
        when(anonymizeDirectory.findById(query))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/internal/anonymizations/anonymize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Should return RFC7807 error for not found exception")
    void testFetchById_WhenThrowsNotFoundException_ShouldReturnProblemDetails() throws Exception {
        // Given
        FetchByIdQuery query = new FetchByIdQuery(testId);
        when(anonymizeDirectory.findById(query))
            .thenThrow(new NotFoundException("anonymize.notfound", Map.of("id", testId)));

        // When & Then
        mockMvc.perform(get("/internal/anonymizations/anonymize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("anonymize.notfound"))
                .andExpect(jsonPath("$.instance").value("/internal/anonymizations/anonymize"))
                .andExpect(jsonPath("$.key").value("anonymize.notfound"));
    }

    // ===== FETCH BY PARAMS TESTS =====

    @Test
    @DisplayName("Should fetch by params successfully")
    void testFetchByParams_ShouldFetchSuccessfully() throws Exception {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(testId, testPersonRef);
        List<AnonymizeResponse> expectedResponses = List.of(
            new AnonymizeResponse(testId, testPersonRef)
        );

        when(anonymizeDirectory.findByParams(query))
            .thenReturn(expectedResponses);

        // When & Then
        mockMvc.perform(get("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].anonymizationId").value(testId.toString()))
                .andExpect(jsonPath("$[0].personRef").value(testPersonRef.toString()));
    }

    @Test
    @DisplayName("Should return empty list when no matches found")
    void testFetchByParams_WhenNoMatches_ShouldReturnEmptyList() throws Exception {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(null, null);
        when(anonymizeDirectory.findByParams(query))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should handle null params")
    void testFetchByParams_WithNullParams_ShouldHandleCorrectly() throws Exception {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(null, null);
        when(anonymizeDirectory.findByParams(query))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ===== PING TESTS =====

    @Test
    @DisplayName("Should return pong for ping")
    void testPing_ShouldReturnPong() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/anonymizations/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("PONG!"));
    }


    // ===== PATH ROUTING TESTS =====

    @Test
    @DisplayName("Should return 404 for non-existent endpoints")
    void testRouting_ShouldReturn404ForNonExistentEndpoints() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/anonymizations/nonexistent"))
                .andExpect(status().isBadRequest());
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle validation errors with RFC7807 format")
    void testErrorHandling_ShouldHandleValidationErrorsWithRfc7807() throws Exception {
        // Given
        CreateAnonymizeCmd invalidCmd = new CreateAnonymizeCmd(null);

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCmd)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/internal/anonymizations"));
    }

    @Test
    @DisplayName("Should handle unexpected exceptions with RFC7807 format")
    void testErrorHandling_ShouldHandleUnexpectedExceptions() throws Exception {
        // Given
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(testPersonRef);
        when(anonymizeDirectory.create(cmd))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }

    // ===== BOUNDARY VALUE TESTS =====

    @Test
    @DisplayName("Should handle maximum UUID values")
    void testBoundaryValues_WithMaxUuid_ShouldHandleCorrectly() throws Exception {
        // Given
        UUID maxId = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        UUID maxPersonRef = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(maxPersonRef);
        AnonymizeResponse expectedResponse = new AnonymizeResponse(maxId, maxPersonRef);

        when(anonymizeDirectory.create(cmd))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anonymizationId").value(maxId.toString()))
                .andExpect(jsonPath("$.personRef").value(maxPersonRef.toString()));
    }

    // ===== MULTIPLE REQUESTS TESTS =====

    @Test
    @DisplayName("Should handle multiple concurrent requests")
    void testConcurrency_ShouldHandleMultipleRequests() throws Exception {
        // Given
        CreateAnonymizeCmd cmd1 = new CreateAnonymizeCmd(UUID.randomUUID());
        CreateAnonymizeCmd cmd2 = new CreateAnonymizeCmd(UUID.randomUUID());

        when(anonymizeDirectory.create(any()))
            .thenReturn(new AnonymizeResponse(UUID.randomUUID(), UUID.randomUUID()));

        // When & Then
        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/internal/anonymizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd2)))
                .andExpect(status().isCreated());

        verify(anonymizeDirectory, times(2)).create(any());
    }



}
