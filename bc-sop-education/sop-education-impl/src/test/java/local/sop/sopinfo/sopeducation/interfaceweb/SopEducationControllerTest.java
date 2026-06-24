package local.sop.sopinfo.sopeducation.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.sopinfo.infrastructure.security.DisableSecurity;
import local.sop.sopinfo.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sopeducation.application.api.SopEducationDirectory;
import local.sop.sopinfo.sopeducation.application.api.dto.CreateSopEducationCmd;
import local.sop.sopinfo.sopeducation.application.api.dto.CreatedSopEducationResult;
import local.sop.sopinfo.sopeducation.application.api.dto.SopEducationResponse;
import local.sop.sopinfo.sopeducation.application.api.dto.ToggleActivateSopEducationCmd;

@WebMvcTest(SopEducationController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SopEducationControllerTest {

    private static final String URL = "/internal/sop-educations";

    private static final UUID SOP_REF       = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF  = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, EDUCATION_REF);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SopEducationDirectory directory;

    // ── POST /internal/sop-educations ─────────────────────────────────────────

    @Test
    void create_shouldReturn201_withLocationHeader_whenCommandIsValid() throws Exception {
        CreatedSopEducationResult result = new CreatedSopEducationResult(VALID_KEY);

        when(directory.create(any(CreateSopEducationCmd.class))).thenReturn(result);

        CreateSopEducationCmd cmd = new CreateSopEducationCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/internal/sop-educations/sop-ref/" + SOP_REF + "/education-ref/" + EDUCATION_REF))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void create_shouldReturn400_whenCommandBodyIsMissing() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404_whenServiceThrowsNotFoundException() throws Exception {
        when(directory.create(any(CreateSopEducationCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        CreateSopEducationCmd cmd = new CreateSopEducationCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /internal/sop-educations/toggle-active ────────────────────────────

    @Test
    void toggleActive_shouldReturn200_withUpdatedResponse_whenCommandIsValid() throws Exception {
        SopEducationResponse response = new SopEducationResponse(
                VALID_KEY, false, LocalDateTime.now().minusDays(1));

        when(directory.toggleActive(any(ToggleActivateSopEducationCmd.class))).thenReturn(response);

        ToggleActivateSopEducationCmd cmd = new ToggleActivateSopEducationCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleActive_shouldReturn400_whenCommandBodyIsMissing() throws Exception {
        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleActive_shouldReturn404_whenServiceThrowsNotFoundException() throws Exception {
        when(directory.toggleActive(any(ToggleActivateSopEducationCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        ToggleActivateSopEducationCmd cmd = new ToggleActivateSopEducationCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── GET /internal/sop-educations/sop-ref/{sopId}/education-ref/{educationId}

    @Test
    void findById_shouldReturn200_withResponse_whenEntityExists() throws Exception {
        SopEducationResponse response = new SopEducationResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.of(response));

        mockMvc.perform(get(URL + "/sop-ref/{sopId}/education-ref/{educationId}",
                        SOP_REF, EDUCATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void findById_shouldReturn404_whenEntityDoesNotExist() throws Exception {
        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/sop-ref/{sopId}/education-ref/{educationId}",
                        SOP_REF, EDUCATION_REF))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn400_whenSopIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/sop-ref/{sopId}/education-ref/{educationId}",
                        "not-a-uuid", EDUCATION_REF))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturn400_whenEducationIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/sop-ref/{sopId}/education-ref/{educationId}",
                        SOP_REF, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/sop-educations/by-sop/{sopId} ───────────────────────────

    @Test
    void findBySopId_shouldReturn200_withList_whenResultsExist() throws Exception {
        SopEducationResponse response = new SopEducationResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getBySopRef(SOP_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/by-sop/{sopId}", SOP_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void findBySopId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getBySopRef(SOP_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/by-sop/{sopId}", SOP_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findBySopId_shouldReturn400_whenSopIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/by-sop/{sopId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/sop-educations/by-education/{educationId} ──────────────

    @Test
    void findByEducationId_shouldReturn200_withList_whenResultsExist() throws Exception {
        SopEducationResponse response = new SopEducationResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByEducationRef(EDUCATION_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/by-education/{educationId}", EDUCATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void findByEducationId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByEducationRef(EDUCATION_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/by-education/{educationId}", EDUCATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByEducationId_shouldReturn400_whenEducationIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/by-education/{educationId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
