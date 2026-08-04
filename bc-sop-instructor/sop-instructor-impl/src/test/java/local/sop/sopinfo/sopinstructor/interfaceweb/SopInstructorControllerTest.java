package local.sop.sopinfo.sopinstructor.interfaceweb;

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

import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sopinstructor.application.api.SopInstructorDirectory;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreatedSopInstructorResult;
import local.sop.sopinfo.sopinstructor.application.api.dto.SopInstructorResponse;
import local.sop.sopinfo.sopinstructor.application.api.dto.ToggleActivateSopInstructorCmd;

@WebMvcTest(SopInstructorController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SopInstructorControllerTest {

    private static final String URL = "/internal/sop-instructors";

    private static final UUID SOP_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, INSTRUCTOR_REF);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SopInstructorDirectory directory;

    // ── POST /internal/sop-instructors ─────────────────────────────────────────

    @Test
    void create_shouldReturn201_withLocationHeader_whenCommandIsValid() throws Exception {
        CreatedSopInstructorResult result = new CreatedSopInstructorResult(VALID_KEY);

        when(directory.create(any(CreateSopInstructorCmd.class))).thenReturn(result);

        CreateSopInstructorCmd cmd = new CreateSopInstructorCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/internal/sop-instructors/sop-ref/" + SOP_REF + "/instructor-ref/" + INSTRUCTOR_REF))
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
        when(directory.create(any(CreateSopInstructorCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        CreateSopInstructorCmd cmd = new CreateSopInstructorCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /internal/sop-instructors/toggle-active ────────────────────────────

    @Test
    void toggleActive_shouldReturn200_withUpdatedResponse_whenCommandIsValid() throws Exception {
        SopInstructorResponse response = new SopInstructorResponse(
                VALID_KEY, false, LocalDateTime.now().minusDays(1));

        when(directory.toggleActive(any(ToggleActivateSopInstructorCmd.class))).thenReturn(response);

        ToggleActivateSopInstructorCmd cmd = new ToggleActivateSopInstructorCmd(VALID_KEY, false);

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
        when(directory.toggleActive(any(ToggleActivateSopInstructorCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        ToggleActivateSopInstructorCmd cmd = new ToggleActivateSopInstructorCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── GET /internal/sop-instructors/sop-ref/{sopId}/instructor-ref/{instructorId}

    @Test
    void findById_shouldReturn200_withResponse_whenEntityExists() throws Exception {
        SopInstructorResponse response = new SopInstructorResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.of(response));

        mockMvc.perform(get(URL + "/sop-ref/{sopId}/instructor-ref/{instructorId}",
                        SOP_REF, INSTRUCTOR_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void findById_shouldReturn404_whenEntityDoesNotExist() throws Exception {
        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/sop-ref/{sopId}/instructor-ref/{instructorId}",
                        SOP_REF, INSTRUCTOR_REF))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn400_whenSopIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/sop-ref/{sopId}/instructor-ref/{instructorId}",
                        "not-a-uuid", INSTRUCTOR_REF))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturn400_whenInstructorIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/sop-ref/{sopId}/instructor-ref/{instructorId}",
                        SOP_REF, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/sop-instructors/by-sop/{sopId} ───────────────────────────

    @Test
    void findBySopId_shouldReturn200_withList_whenResultsExist() throws Exception {
        SopInstructorResponse response = new SopInstructorResponse(
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

    // ── GET /internal/sop-instructors/by-instructor/{instructorId} ──────────────

    @Test
    void findByInstructorId_shouldReturn200_withList_whenResultsExist() throws Exception {
        SopInstructorResponse response = new SopInstructorResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/by-instructor/{instructorId}", INSTRUCTOR_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void findByInstructorId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/by-instructor/{instructorId}", INSTRUCTOR_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByInstructorId_shouldReturn400_whenInstructorIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/by-instructor/{instructorId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

}
