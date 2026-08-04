package local.sop.sopinfo.educationline.saga.application.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import local.sop.sopinfo.educationline.saga.application.api.EducationLineSagaDirectory;
import local.sop.sopinfo.educationline.saga.application.api.dto.*;
import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;


@WebMvcTest(EducationLineSagaController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class EducationLineSagaControllerTest {
    
    //#region Test Setup

    private static final String BASE_URL = "/internal/saga/educationlines";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EducationLineSagaDirectory educationLineDirectory;

    private UUID educationLineId;
    private CreateEducationLineCmd validCreateCmd;
    private UpdateEducationLineDurationCmd validUpdateDurationCmd;
    private UpdateEducationLineNameCmd validUpdateNameCmd;
    private EducationLineResponse validResponse;
	private CreateAuditlogCmd validCreateAuditlogCmd;

    //#endregion

    //#region Test Setup Methods

    @BeforeEach
    void setUp() {
        educationLineId = UUID.randomUUID();

        validCreateCmd = new CreateEducationLineCmd(
            "TEST_NAME",
            3,
            6,
            0,
            UUID.randomUUID(),
            UUID.randomUUID(),
            ActorType.SYSTEM,
            Severity.INFO,
            "TEST_SYSTEM",
            "TEST_SERVICE",
            "TEST_COMPONENT",
            "TEST_DATA",
            "TEST_DESCRIPTION"
        );

        validUpdateDurationCmd = new UpdateEducationLineDurationCmd(
            3,
            6,
            0,
            UUID.randomUUID(),
            ActorType.SYSTEM,
            Severity.INFO,
            "TEST_SYSTEM",
            "TEST_SERVICE",
            "TEST_COMPONENT",
            "TEST_DATA",
            "TEST_DESCRIPTION"
        );

        validUpdateNameCmd = new UpdateEducationLineNameCmd(
            "TEST_NAME",
            UUID.randomUUID(),
            ActorType.SYSTEM,
            Severity.INFO,
            "TEST_SYSTEM",
            "TEST_SERVICE",
            "TEST_COMPONENT",
            "TEST_DATA",
            "TEST_DESCRIPTION"
        );

        validResponse = new EducationLineResponse(
            educationLineId,
            "TEST_NAME",
            3,
            6,
            0,
            UUID.randomUUID(),
            null,
            true
        );

        validCreateAuditlogCmd = new CreateAuditlogCmd(
                UUID.randomUUID(),
                ActorType.SYSTEM,
                Severity.INFO,
                "TEST_SYSTEM",
                "TEST_SERVICE",
                "TEST_COMPONENT",
                "TEST_DATA",
                "TEST_DESCRIPTION"
        );
    }

    //#endregion

    /* =========================================================== *
     *  Happy path
     * =========================================================== */
    
    //#region Universal endpoint test
    @Nested
    class UniversalEndpointTests {
        @BeforeEach
        void directoryReturnOk() {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenReturn(validResponse);
            
            when(educationLineDirectory.updateEducationLineDuration(any(UUID.class),
                    any(UpdateEducationLineDurationCmd.class)))
                    .thenReturn(validResponse);
            
            when(educationLineDirectory.updateEducationLineName(any(UUID.class),
                    any(UpdateEducationLineNameCmd.class)))
                    .thenReturn(validResponse);
            
            when(educationLineDirectory.deactivateEducationLine(any(UUID.class), any(CreateAuditlogCmd.class)))
                    .thenReturn(validResponse);
                
            when(educationLineDirectory.activateEducationLine(any(UUID.class), any(CreateAuditlogCmd.class)))
                    .thenReturn(validResponse);
        }
        
        /*
        Should Return 201 Created when request is valid
        Should Return 200 OK when request is valid
        */

        @Test
        void create_shouldReturn201_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isCreated());
        }

        @Test
        void updateDuration_shouldReturn200_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(status().isOk());
        }

        @Test
        void updateName_shouldReturn200_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(status().isOk());
        }

        @Test
        void deactivate_shouldReturn200_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk());
        }
        
        @Test
        void activate_shouldReturn200_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk());
        }

        // Should Return Location header with URL to created resource when request is valid

        @Test
        void create_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(header().string("Location", BASE_URL + "/" + educationLineId));
        }

        @Test
        void updateDuration_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Location", BASE_URL + "/" + educationLineId));
        }
        
        @Test
        void updateName_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Location", BASE_URL + "/" + educationLineId));
        }
        
        @Test
        void deactivate_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Location", BASE_URL + "/" + educationLineId));
        }
        
        @Test
        void activate_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Location", BASE_URL + "/" + educationLineId));
        }
        
        // Should Return application/json content type when request is valid
        
        @Test
        void create_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void updateDuration_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void updateName_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void deactivate_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void activate_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
        
        // Should Delegate to directory when request is valid

        @Test
        void create_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isCreated());

            verify(educationLineDirectory).createEducationLine(any(CreateEducationLineCmd.class));
        }
        
        @Test
        void updateDuration_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(status().isOk());

            verify(educationLineDirectory).updateEducationLineDuration(eq(educationLineId),
                    any(UpdateEducationLineDurationCmd.class));
        }
        
        @Test
        void updateName_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(status().isOk());

            verify(educationLineDirectory).updateEducationLineName(eq(educationLineId),
                    any(UpdateEducationLineNameCmd.class));
        }

        @Test
        void deactivate_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk());

            verify(educationLineDirectory).deactivateEducationLine(eq(educationLineId), any(CreateAuditlogCmd.class));
        }

        @Test
        void activate_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isOk());

            verify(educationLineDirectory).activateEducationLine(eq(educationLineId), any(CreateAuditlogCmd.class));
        }
    }
    //#endregion

    //#region Create Cmd Happy Path
    @Nested
    class CreateEducationLineHappyPath {
        @BeforeEach
        void directoryReturnOk() {
            when(educationLineDirectory.createEducationLine(any())).thenReturn(validResponse);
        }

        @Test
        void create_shouldReturnEducationLineId_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.id").value(educationLineId.toString()));
        }
        
        @Test
        void create_shouldReturnEducationLineName_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.name").value(validResponse.name()));
        }
        @Test
        void create_shouldReturnEducationLineDurationYears_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.durationYears").value(validResponse.durationYears()));
        }
        @Test
        void create_shouldReturnEducationLineDurationMonths_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.durationMonths").value(validResponse.durationMonths()));
        }
        @Test
        void create_shouldReturnEducationLineDurationDays_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.durationDays").value(validResponse.durationDays()));
        }
        @Test
        void create_shouldReturnEducationLineEducationRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.educationRef").value(validResponse.educationRef().toString()));
        }
    }
    //#endregion

    //#region Update Name Cmd Happy Path
    @Nested
    class UpdateEducationLineNameHappyPath {
        @BeforeEach
        void directoryReturnOk() {
            when(educationLineDirectory.updateEducationLineName(any(UUID.class),
                    any(UpdateEducationLineNameCmd.class)))
                    .thenReturn(validResponse);
        }

        @Test
        void updateName_shouldReturnEducationLineId_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(jsonPath("$.id").value(educationLineId.toString()));
        }
        
        @Test
        void updateName_shouldReturnEducationLineName_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(jsonPath("$.name").value(validResponse.name()));
        }

        @Test
        void updateName_shouldReturnEducationLineDurationYears_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(jsonPath("$.durationYears").value(validResponse.durationYears()));
        }

        @Test
        void updateName_shouldReturnEducationLineDurationMonths_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(jsonPath("$.durationMonths").value(validResponse.durationMonths()));
        }

        @Test
        void updateName_shouldReturnEducationLineDurationDays_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(jsonPath("$.durationDays").value(validResponse.durationDays()));
        }

        @Test
        void updateName_shouldReturnEducationLineEducationRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateNameCmd)))
                    .andExpect(jsonPath("$.educationRef").value(validResponse.educationRef().toString()));
        }

    }
    //#endregion

    //#region Update Duration Cmd Happy Path
    @Nested
    class UpdateEducationLineDurationHappyPath {
        @BeforeEach
        void directoryReturnOk() {
            when(educationLineDirectory.updateEducationLineDuration(any(UUID.class),
                    any(UpdateEducationLineDurationCmd.class)))
                    .thenReturn(validResponse);
        }

        @Test
        void updateDuration_shouldReturnEducationLineId_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(jsonPath("$.id").value(educationLineId.toString()));
        }
        
        @Test
        void updateDuration_shouldReturnEducationLineName_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(jsonPath("$.name").value(validResponse.name()));
        }

        @Test
        void updateDuration_shouldReturnEducationLineDurationYears_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(jsonPath("$.durationYears").value(validResponse.durationYears()));
        }

        @Test
        void updateDuration_shouldReturnEducationLineDurationMonths_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(jsonPath("$.durationMonths").value(validResponse.durationMonths()));
        }

        @Test
        void updateDuration_shouldReturnEducationLineDurationDays_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(jsonPath("$.durationDays").value(validResponse.durationDays()));
        }

        @Test
        void updateDuration_shouldReturnEducationLineEducationRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateDurationCmd)))
                    .andExpect(jsonPath("$.educationRef").value(validResponse.educationRef().toString()));
        }
    }
    //#endregion

    //#region Deactivate Happy Path
    @Nested
    class DeactivateEducationLineHappyPath {
        @BeforeEach
        void directoryReturnOk() {
            when(educationLineDirectory.deactivateEducationLine(any(UUID.class), any(CreateAuditlogCmd.class)))
                    .thenReturn(validResponse);
        }

        @Test
        void deactivate_shouldReturnEducationLineId_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.id").value(educationLineId.toString()));
        }

        @Test
        void deactivate_shouldReturnEducationLineName_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.name").value(validResponse.name()));
        }

        @Test
        void deactivate_shouldReturnEducationLineDurationYears_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.durationYears").value(validResponse.durationYears()));
        }

        @Test
        void deactivate_shouldReturnEducationLineDurationMonths_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.durationMonths").value(validResponse.durationMonths()));
        }

        @Test
        void deactivate_shouldReturnEducationLineDurationDays_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.durationDays").value(validResponse.durationDays()));
        }

        @Test
        void deactivate_shouldReturnEducationLineEducationRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.educationRef").value(validResponse.educationRef().toString()));
        }
    }
    //#endregion

    //#region Activate Happy Path
    @Nested
    class ActivateEducationLineHappyPath {
        @BeforeEach
        void directoryReturnOk() {
            when(educationLineDirectory.activateEducationLine(any(UUID.class), any(CreateAuditlogCmd.class)))
                    .thenReturn(validResponse);
        }

        @Test
        void activate_shouldReturnEducationLineId_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.durationYears").value(validResponse.durationYears()));
        }

        @Test
        void activate_shouldReturnEducationLineDurationMonths_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.durationMonths").value(validResponse.durationMonths()));
        }

        @Test
        void activate_shouldReturnEducationLineDurationDays_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.durationDays").value(validResponse.durationDays()));
        }

        @Test
        void activate_shouldReturnEducationLineEducationRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.educationRef").value(validResponse.educationRef().toString()));
        }
    }
    //#endregion

    /* =========================================================== *
     *  Validation — @Valid on @RequestBody
     * =========================================================== */

    //#region Create Education Line Validation
    @Nested
    class CreateEducationLineValidation {
        @Test
        void create_shouldReturn400_whenBodyIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenBodyIsMalformedJson() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{not valid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenNameIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineName(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDurationYearsIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDurationYears(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDurationMonthsIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDurationMonths(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDurationDaysIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDurationDays(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenEducationRefIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineEducationRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenEducationLineNameExceeds100Characters() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineName("x".repeat(101)))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenEducationLineDurationYearsIsNegative() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDurationYears(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenEducationLineDurationMonthsIsNegative() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDurationMonths(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenEducationLineDurationDaysIsNegative() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDurationDays(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenActorRefIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineActorRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenActorTypeIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineActorType(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenSeverityIsNull() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineSeverity(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenOriginSystemIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineOriginSystem("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenOriginServiceIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineOriginService("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenOriginComponentIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineOriginComponent("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDataIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineData("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDescriptionIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineDescription("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineName(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void create_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withCreateEducationLineName(null))));

            verify(educationLineDirectory, never()).createEducationLine(any());
        }

    }
    //#endregion
    
    //#region Update Name Education Line Validation
    
    @Nested
    class UpdateEducationLineNameValidation {
        @Test
        void updateName_shouldReturn400_whenBodyIsMissing() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenBodyIsMalformedJson() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{not valid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenNameIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineName(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenDurationYearsIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationYears(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenDurationMonthsIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationMonths(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenDurationDaysIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDays(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenEducationLineNameExceeds100Characters() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineName("x".repeat(101)))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenEducationLineDurationYearsIsNegative() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationYears(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenEducationLineDurationMonthsIsNegative() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationMonths(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenEducationLineDurationDaysIsNegative() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDays(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenActorRefIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameActorRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenActorTypeIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameActorType(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenSeverityIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameSeverity(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenOriginSystemIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameOriginSystem("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenOriginServiceIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameOriginService("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenOriginComponentIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameOriginComponent("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenDataIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameData("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturn400_whenDescriptionIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineNameDescription("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateName_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineName(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void UpdateName_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineName(null))));

            verify(educationLineDirectory, never()).createEducationLine(any());
        }

    }

    //#endregion

    //#region Update Duration Education Line Validation

    @Nested
    class UpdateEducationLineDurationValidation {
        
        @Test
        void updateDuration_shouldReturn400_whenBodyIsMissing() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenBodyIsMalformedJson() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{not valid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenDurationYearsIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationYears(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenDurationMonthsIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationMonths(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenDurationDaysIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDays(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenEducationLineNameExceeds100Characters() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineName("x".repeat(101)))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenEducationLineDurationYearsIsNegative() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationYears(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenEducationLineDurationMonthsIsNegative() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationMonths(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenEducationLineDurationDaysIsNegative() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDays(-1))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenActorRefIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationActorRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenActorTypeIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationActorType(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenSeverityIsNull() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationSeverity(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenOriginSystemIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationOriginSystem("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenOriginServiceIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationOriginService("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenOriginComponentIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationOriginComponent("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenDataIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationData("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDuration_shouldReturn400_whenDescriptionIsBlank() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDescription("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void UpdateDurationYears_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationYears(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }
        
        @Test
        void UpdateDurationMonths_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationMonths(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void UpdateDurationDays_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDays(null))))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void UpdateDurationYears_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationYears(null))));

            verify(educationLineDirectory, never()).createEducationLine(any());
        }
        
        @Test
        void UpdateDurationMonths_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationMonths(null))));

            verify(educationLineDirectory, never()).createEducationLine(any());
        }
        
        @Test
        void UpdateDurationDays_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(withUpdateEducationLineDurationDays(null))));

            verify(educationLineDirectory, never()).createEducationLine(any());
        }
    }

    //#endregion

    /* =========================================================== *
     *  Service / domain layer failures — RFC 7807 ProblemDetail
     * =========================================================== */
    
    @Nested
    class CreateEducationLineServiceFailures {
        //#region Create Education Line Service Failures
        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenThrow(new ConflictException("educationline.notcreated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isConflict());
        }
 
        @Test
        void create_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenThrow(new ConflictException("educationline.notcreated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }
 
        @Test
        void create_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenThrow(new ConflictException("educationline.notcreated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.key").value("educationline.notcreated"));
        }
 
        @Test
        void create_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenThrow(new ConflictException("auditlog.notcreated",
                            Map.of("object", "auditlog")));
 
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notcreated"));
        }
 
        @Test
        void create_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isInternalServerError());
        }
 
        @Test
        void create_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(educationLineDirectory.createEducationLine(any()))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }

        //#endregion
        
        //#region Update Education Line Name Service Failures
        
        @Test
        void updateName_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.updateEducationLineName(any(), any()))
                    .thenThrow(new ConflictException("educationline.notupdated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isConflict());
        }
 
        @Test
        void updateName_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.updateEducationLineName(any(), any()))
                    .thenThrow(new ConflictException("educationline.notupdated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }
 
        @Test
        void updateName_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.updateEducationLineName(any(), any()))
                    .thenThrow(new ConflictException("educationline.notupdated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.key").value("educationline.notupdated"));
        }
 
        @Test
        void updateName_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(educationLineDirectory.updateEducationLineName(any(), any()))
                    .thenThrow(new ConflictException("auditlog.notcreated",
                            Map.of("object", "auditlog")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notcreated"));
        }
 
        @Test
        void updateName_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(educationLineDirectory.updateEducationLineName(any(), any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isInternalServerError());
        }
 
        @Test
        void updateName_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(educationLineDirectory.updateEducationLineName(any(), any()))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(put(BASE_URL + "/{id}/name", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }
        
        //#endregion
        
        //#region Update Education Line Duration Service Failures

        @Test
        void updateDuration_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
        when(educationLineDirectory.updateEducationLineDuration(any(), any()))
                .thenThrow(new ConflictException("educationline.notupdated",
                        Map.of("object", "educationline")));

        mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateCmd)))
                .andExpect(status().isConflict());
        }
 
        @Test
        void updateDuration_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.updateEducationLineDuration(any(), any()))
                    .thenThrow(new ConflictException("educationline.notupdated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }
 
        @Test
        void updateDuration_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.updateEducationLineDuration(any(), any()))
                    .thenThrow(new ConflictException("educationline.notupdated",
                            Map.of("object", "educationline")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.key").value("educationline.notupdated"));
        }
 
        @Test
        void updateDuration_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(educationLineDirectory.updateEducationLineDuration(any(), any()))
                    .thenThrow(new ConflictException("auditlog.notcreated",
                            Map.of("object", "auditlog")));
 
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notcreated"));
        }
 
        @Test
        void updateDuration_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(educationLineDirectory.updateEducationLineDuration(any(), any()))
                    .thenThrow(new RuntimeException("unexpected failure"));
 
            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(status().isInternalServerError());
        }
 
        @Test
        void updateDuration_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(educationLineDirectory.updateEducationLineDuration(any(), any()))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(put(BASE_URL + "/{id}/duration", educationLineId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }

        //#endregion

        //#region Deactivate Education Line Service Failures

        @Test
        void deactivate_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.deactivateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("educationline.notdeactivated",
                            Map.of("object", "educationline")));

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isConflict());
        }

        @Test
        void deactivate_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.deactivateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("educationline.notdeactivated",
                            Map.of("object", "educationline")));

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }

        @Test
        void deactivate_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.deactivateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("educationline.notdeactivated",
                            Map.of("object", "educationline")));

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.key").value("educationline.notdeactivated"));
        }

        @Test
        void deactivate_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(educationLineDirectory.deactivateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("auditlog.notdeactivated",
                            Map.of("object", "auditlog")));

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notdeactivated"));
        }

        @Test
        void deactivate_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(educationLineDirectory.deactivateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void deactivate_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(educationLineDirectory.deactivateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(put(BASE_URL + "/{id}/deactivate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }
        
        //#endregion

        //#region Activate Education Line Service Failures

        @Test
        void activate_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.activateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("educationline.notactivated",
                            Map.of("object", "educationline")));

            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isConflict());
        }

        @Test
        void activate_shouldReturnConflictProblemDetailType_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.activateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("educationline.notactivated",
                            Map.of("object", "educationline")));

            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }

        @Test
        void activate_shouldReturnMessageKey_whenDirectoryThrowsConflictException() throws Exception {
            when(educationLineDirectory.activateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("educationline.notactivated",
                            Map.of("object", "educationline")));

            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.key").value("educationline.notactivated"));
        }

        @Test
        void activate_shouldReturn409_whenDirectoryThrowsConflictForAuditlog() throws Exception {
            when(educationLineDirectory.activateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new ConflictException("auditlog.notactivated",
                            Map.of("object", "auditlog")));

            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.key").value("auditlog.notactivated"));
        }

        @Test
        void activate_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
            when(educationLineDirectory.activateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void activate_shouldReturnInternalErrorProblemDetailType_onUnexpectedException() throws Exception {
            when(educationLineDirectory.activateEducationLine(any(), any(CreateAuditlogCmd.class)))
                    .thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(put(BASE_URL + "/{id}/activate", educationLineId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateAuditlogCmd)))
                    .andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }

        //#endregion
    }

    /* =========================================================== *
     *  Builder helpers — produce clean variants of validCmd
     * =========================================================== */
    
    //#region Create Cmd builders
    private CreateEducationLineCmd withCreateEducationLineName(String name) {
        return new CreateEducationLineCmd(
                name,
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }

    private CreateEducationLineCmd withCreateEducationLineDurationYears(Integer durationYears) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                durationYears,
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }

    private CreateEducationLineCmd withCreateEducationLineDurationMonths(Integer durationMonths) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                durationMonths,
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }

    private CreateEducationLineCmd withCreateEducationLineDurationDays(Integer durationDays) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                durationDays,
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }

    private CreateEducationLineCmd withCreateEducationLineEducationRef(UUID educationRef) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                educationRef,
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }
    
    private CreateEducationLineCmd withCreateEducationLineActorRef(UUID actorRef) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                actorRef,
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }

    private CreateEducationLineCmd withCreateEducationLineActorType(ActorType actorType) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                actorType,
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }
    
    private CreateEducationLineCmd withCreateEducationLineSeverity(Severity severity) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                severity,
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }
    
    private CreateEducationLineCmd withCreateEducationLineOriginSystem(String originSystem) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                originSystem,
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }
    
    private CreateEducationLineCmd withCreateEducationLineOriginService(String originService) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                originService,
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                validCreateCmd.description());
    }
    
    private CreateEducationLineCmd withCreateEducationLineOriginComponent(String originComponent) {
        return new CreateEducationLineCmd(
            validCreateCmd.name(),
            validCreateCmd.durationYears(),
            validCreateCmd.durationMonths(),
            validCreateCmd.durationDays(),
            validCreateCmd.educationRef(),
            validCreateCmd.actorRef(),
            validCreateCmd.actorType(),
            validCreateCmd.severity(),
            validCreateCmd.originSystem(),
            validCreateCmd.originService(),
            originComponent,
            validCreateCmd.data(),
            validCreateCmd.description());
    }

    private CreateEducationLineCmd withCreateEducationLineData(String data) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                data,
                validCreateCmd.description());
    }
    
    private CreateEducationLineCmd withCreateEducationLineDescription(String description) {
        return new CreateEducationLineCmd(
                validCreateCmd.name(),
                validCreateCmd.durationYears(),
                validCreateCmd.durationMonths(),
                validCreateCmd.durationDays(),
                validCreateCmd.educationRef(),
                validCreateCmd.actorRef(),
                validCreateCmd.actorType(),
                validCreateCmd.severity(),
                validCreateCmd.originSystem(),
                validCreateCmd.originService(),
                validCreateCmd.originComponent(),
                validCreateCmd.data(),
                description);
    }
    //#endregion

    //#region Update Name Cmd builders
    private UpdateEducationLineNameCmd withUpdateEducationLineName(String name) {
        return new UpdateEducationLineNameCmd(
                name,
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }
    
    private UpdateEducationLineNameCmd withUpdateEducationLineNameActorRef(UUID actorRef) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                actorRef,
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameActorType(ActorType actorType) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                actorType,
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameSeverity(Severity severity) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                severity,
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameOriginSystem(String originSystem) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                originSystem,
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameOriginService(String originService) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                originService,
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameOriginComponent(String originComponent) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                originComponent,
                validUpdateNameCmd.data(),
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameData(String data) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                data,
                validUpdateNameCmd.description());
    }

    private UpdateEducationLineNameCmd withUpdateEducationLineNameDescription(String description) {
        return new UpdateEducationLineNameCmd(
                validUpdateNameCmd.name(),
                validUpdateNameCmd.actorRef(),
                validUpdateNameCmd.actorType(),
                validUpdateNameCmd.severity(),
                validUpdateNameCmd.originSystem(),
                validUpdateNameCmd.originService(),
                validUpdateNameCmd.originComponent(),
                validUpdateNameCmd.data(),
                description);
    }
    //#endregion

    //#region Update Duration Cmd builders
    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationYears(Integer durationYears) {
        return new UpdateEducationLineDurationCmd(
                durationYears,
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationMonths(Integer durationMonths) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                durationMonths,
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationDays(Integer durationDays) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                durationDays,
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationActorRef(UUID actorRef) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                actorRef,
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationActorType(ActorType actorType) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                actorType,
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationSeverity(Severity severity) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                severity,
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationOriginSystem(String originSystem) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                originSystem,
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationOriginService(String originService) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                originService,
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationOriginComponent(String originComponent) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                originComponent,
                validUpdateDurationCmd.data(),
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationData(String data) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                data,
                validUpdateDurationCmd.description());
    }

    private UpdateEducationLineDurationCmd withUpdateEducationLineDurationDescription(String description) {
        return new UpdateEducationLineDurationCmd(
                validUpdateDurationCmd.durationYears(),
                validUpdateDurationCmd.durationMonths(),
                validUpdateDurationCmd.durationDays(),
                validUpdateDurationCmd.actorRef(),
                validUpdateDurationCmd.actorType(),
                validUpdateDurationCmd.severity(),
                validUpdateDurationCmd.originSystem(),
                validUpdateDurationCmd.originService(),
                validUpdateDurationCmd.originComponent(),
                validUpdateDurationCmd.data(),
                description);
    }
    //#endregion
}
