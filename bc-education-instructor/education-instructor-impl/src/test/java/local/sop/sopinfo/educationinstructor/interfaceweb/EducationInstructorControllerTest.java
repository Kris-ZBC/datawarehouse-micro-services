package local.sop.sopinfo.educationinstructor.interfaceweb;

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

import local.sop.sopinfo.educationinstructor.application.api.EducationInstructorDirectory;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreateEducationInstructorCmd;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreatedEducationInstructorResult;
import local.sop.sopinfo.educationinstructor.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.educationinstructor.application.api.dto.ToggleActivateEducationInstructorCmd;
import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;


@WebMvcTest(EducationInstructorController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EducationInstructorControllerTest {

    private static final String URL = "/internal/education-instructors";

    private static final UUID EDUCATION_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(EDUCATION_REF, INSTRUCTOR_REF);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EducationInstructorDirectory directory;

    // ── POST /internal/education-instructors ─────────────────────────────────────────

    @Test
    void create_shouldReturn201_withLocationHeader_whenCommandIsValid() throws Exception {
        CreatedEducationInstructorResult result = new CreatedEducationInstructorResult(VALID_KEY);

        when(directory.create(any(CreateEducationInstructorCmd.class))).thenReturn(result);

        CreateEducationInstructorCmd cmd = new CreateEducationInstructorCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/internal/education-instructors/education-ref/" + EDUCATION_REF + "/instructor-ref/" + INSTRUCTOR_REF))
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
        when(directory.create(any(CreateEducationInstructorCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        CreateEducationInstructorCmd cmd = new CreateEducationInstructorCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /internal/education-instructors/toggle-active ────────────────────────────

    @Test
    void toggleActive_shouldReturn200_withUpdatedResponse_whenCommandIsValid() throws Exception {
        EducationInstructorResponse response = new EducationInstructorResponse(
                VALID_KEY, false, LocalDateTime.now().minusDays(1));

        when(directory.toggleActive(any(ToggleActivateEducationInstructorCmd.class))).thenReturn(response);

        ToggleActivateEducationInstructorCmd cmd = new ToggleActivateEducationInstructorCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void toggleActive_shouldReturn400_whenCommandBodyIsMissing() throws Exception {
        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleActive_shouldReturn404_whenServiceThrowsNotFoundException() throws Exception {
        when(directory.toggleActive(any(ToggleActivateEducationInstructorCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        ToggleActivateEducationInstructorCmd cmd = new ToggleActivateEducationInstructorCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── GET /internal/education-instructors/education-ref/{educationId}/instructor-ref/{instructorId}

    @Test
    void findById_shouldReturn200_withResponse_whenEntityExists() throws Exception {
        EducationInstructorResponse response = new EducationInstructorResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.of(response));

        mockMvc.perform(get(URL + "/education-ref/{educationId}/instructor-ref/{instructorId}",
                        EDUCATION_REF, INSTRUCTOR_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void findById_shouldReturn404_whenEntityDoesNotExist() throws Exception {
        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/education-ref/{educationId}/instructor-ref/{instructorId}",
                        EDUCATION_REF, INSTRUCTOR_REF))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn400_whenEducationIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/education-ref/{educationId}/instructor-ref/{instructorId}",
                        "not-a-uuid", INSTRUCTOR_REF))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturn400_whenInstructorIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/education-ref/{educationId}/instructor-ref/{instructorId}",
                        EDUCATION_REF, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/education-instructors/by-education/{educationId} ───────────────────────────

    @Test
    void findByEducationId_shouldReturn200_withList_whenResultsExist() throws Exception {
        EducationInstructorResponse response = new EducationInstructorResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByEducationRef(EDUCATION_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/education-ref/{educationId}", EDUCATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void findByEducationId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByEducationRef(EDUCATION_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/education-ref/{educationId}", EDUCATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByEducationId_shouldReturn400_whenEducationIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/education-ref/{educationId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/education-instructors/by-instructor/{instructorId} ──────────────

    @Test
    void findByInstructorId_shouldReturn200_withList_whenResultsExist() throws Exception {
        EducationInstructorResponse response = new EducationInstructorResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/instructor-ref/{instructorId}", INSTRUCTOR_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void findByInstructorId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/instructor-ref/{instructorId}", INSTRUCTOR_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByInstructorId_shouldReturn400_whenInstructorIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/instructor-ref/{instructorId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

	// GET all

	@Test
	void getAll_shouldReturn200_withList_whenResultsExist() throws Exception {
		UUID educationRef = UUID.randomUUID();
		UUID instructorRef = UUID.randomUUID();

		EducationInstructorResponse response = 
			new EducationInstructorResponse(
				new CompositeKey(educationRef, instructorRef), true, LocalDateTime.now().minusDays(1));

		when(directory.getAll()).thenReturn(List.of(response));

		mockMvc.perform(get("/internal/education-instructors")) .andExpect(status().isOk());
	}

	@Test
	void getAll_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
		when(directory.getAll()).thenReturn(List.of());

		mockMvc.perform(get("/internal/education-instructors")) .andExpect(status().isOk());
	}

	// ── Compensate endpoints ────────────────────────────────────────────────

	@Test
	void compensateCreateEducationInstructor_returnsOk() throws Exception {
		PayloadCompensateCreate cmd = new PayloadCompensateCreate(String.class,SagaOutcome.COMPENSATE);
		ResponseCompensated expected = new ResponseCompensated(SagaOutcome.COMPENSATE,true);

		when(directory.compensateCreateEducationInstructor(
            any(CompositeKey.class),
            any(Class.class),
            any(SagaOutcome.class)))
            .thenReturn(expected);

		mockMvc.perform(
            post("/internal/education-instructors/education/{educationId}/instructor/{instructorId}/compensate-create",EDUCATION_REF,INSTRUCTOR_REF)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cmd)))
            .andExpect(status().isOk());
	}

	@Test
	void compensateActivateEducationInstructor_returnsOk() throws Exception {
    PayloadCompensateCreate cmd = new PayloadCompensateCreate(String.class, SagaOutcome.COMPENSATE);
	ResponseCompensated expected = new ResponseCompensated(SagaOutcome.COMPENSATE, true);

    when(directory.compensateActivateEducationInstructor(
		any(CompositeKey.class),
		any(Class.class),
		any(SagaOutcome.class)))
		.thenReturn(expected);

    mockMvc.perform(
		post("/internal/education-instructors/education/{educationId}/instructor/{instructorId}/compensate-activate",EDUCATION_REF,INSTRUCTOR_REF)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(cmd)))
		.andExpect(status().isOk());
	}

	@Test
	void compensateDeactivateEducationInstructor_returnsOk() throws Exception {
	PayloadCompensateCreate cmd = new PayloadCompensateCreate(String.class, SagaOutcome.COMPENSATE);
	ResponseCompensated expected = new ResponseCompensated(SagaOutcome.COMPENSATE, true);

	when(directory.compensateDeactivateEducationInstructor(
		any(CompositeKey.class),
		any(Class.class),
		any(SagaOutcome.class)))
		.thenReturn(expected);

	mockMvc.perform(
		post("/internal/education-instructors/education/{educationId}/instructor/{instructorId}/compensate-deactivate",EDUCATION_REF,INSTRUCTOR_REF)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(cmd)))
		.andExpect(status().isOk());
	}


	@Test
	void compensateCreateEducationInstructor_returnsNoContentWhenNull() throws Exception {
	PayloadCompensateCreate cmd = new PayloadCompensateCreate(String.class, SagaOutcome.COMPENSATE);

	when(directory.compensateCreateEducationInstructor(
		any(CompositeKey.class),
		any(Class.class),
		any(SagaOutcome.class)))
		.thenReturn(null);

	mockMvc.perform(
		post("/internal/education-instructors/education/{educationId}/instructor/{instructorId}/compensate-create",EDUCATION_REF,INSTRUCTOR_REF)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(cmd)))
		.andExpect(status().isNoContent());
}

	@Test
	void compensateActivateEducationInstructor_returnsNoContentWhenNull() throws Exception {
	PayloadCompensateCreate cmd = new PayloadCompensateCreate(String.class, SagaOutcome.COMPENSATE);

	when(directory.compensateActivateEducationInstructor(
		any(CompositeKey.class),
		any(Class.class),
		any(SagaOutcome.class)))
		.thenReturn(null);

	mockMvc.perform(
		post("/internal/education-instructors/education/{educationId}/instructor/{instructorId}/compensate-activate",EDUCATION_REF,INSTRUCTOR_REF)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(cmd)))
		.andExpect(status().isNoContent());
}

	@Test
	void compensateDeactivateEducationInstructor_returnsNoContentWhenNull() throws Exception {
	PayloadCompensateCreate cmd = new PayloadCompensateCreate(String.class, SagaOutcome.COMPENSATE);

	when(directory.compensateDeactivateEducationInstructor(
		any(CompositeKey.class),
		any(Class.class),
		any(SagaOutcome.class)))
		.thenReturn(null);

	mockMvc.perform(
		post("/internal/education-instructors/education/{educationId}/instructor/{instructorId}/compensate-deactivate",EDUCATION_REF,INSTRUCTOR_REF)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(cmd)))
		.andExpect(status().isNoContent());
}
}