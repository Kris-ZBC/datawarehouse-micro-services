package local.sop.sopinfo.education.interfaceweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.sop.sopinfo.education.application.api.EducationDirectory;
import local.sop.sopinfo.education.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.application.api.dto.EducationResponse;
import local.sop.sopinfo.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateUpdate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EducationController.class)
@Import(EndpointExceptionHandler.class)
@WithMockUser
class EducationControllerTest {

    private static final String BASE_URL = "/internal/educations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EducationDirectory directory;

    @Test
    void shouldCreateEducation() throws Exception {
        CreateEducationCmd cmd = new CreateEducationCmd("IT Support", "Data and Communication");
        EducationResponse response = new EducationResponse(
                UUID.randomUUID(), "IT Support", "Data and Communication", false);

        when(directory.createEducation(any(CreateEducationCmd.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT Support"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void shouldFindById() throws Exception {
        UUID id = UUID.randomUUID();
        Optional<EducationResponse> response = Optional.ofNullable(new EducationResponse(
                id, "IT Support", "Data and Communication", true));

        when(directory.findById(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(directory.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldActivateEducation() throws Exception {
        UUID id = UUID.randomUUID();
        EducationResponse response = new EducationResponse(
                id, "IT Support", "Data and Communication", true);

        when(directory.activateEducation(id)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}/activate", id)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        verify(directory).activateEducation(id);
    }

    @Test
    void shouldUpdateEducationName() throws Exception {
        UUID id = UUID.randomUUID();
        String newName = "Updated Name";
        EducationResponse response = new EducationResponse(
                id, "Updated Name", "Data and Communication", true);

        when(directory.updateEducationName(any(UUID.class), any(String.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}/name", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void shouldUpdateEducationCategory() throws Exception {
        UUID id = UUID.randomUUID();
        String newCategory = "Updated IT";
        EducationResponse response = new EducationResponse(
                id, "IT Support", "Updated IT", true);

        when(directory.updateEducationCategory(any(UUID.class), any(String.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}/category", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Updated IT"));
    }

    @Test
    void shouldFindAll() throws Exception {
        EducationResponse resp = new EducationResponse(
                UUID.randomUUID(), "IT Support", "Data and Communication", true);
        when(directory.findAll()).thenReturn(List.of(resp));

        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IT Support"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void shouldDeactivateEducation() throws Exception {
        UUID id = UUID.randomUUID();
        EducationResponse response = new EducationResponse(
                id, "IT Support", "Data and Communication", false);

        when(directory.deactivateEducation(id)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}/deactivate", id)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(directory).deactivateEducation(id);
    }

    @Test
    void shouldCompensateCreateEducation() throws Exception {
        UUID id = UUID.randomUUID();
        PayloadCompensateCreate payload = new PayloadCompensateCreate(EducationControllerTest.class, SagaOutcome.COMPENSATED);
        ResponseCompensated response = new ResponseCompensated(SagaOutcome.COMPENSATED, null);

        when(directory.compensateCreateEducation(eq(id), any(), any(SagaOutcome.class)))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{id}/compensate-create", id)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(directory).compensateCreateEducation(eq(id), any(), any(SagaOutcome.class));
    }

    @Test
    void shouldCompensateUpdateEducationName() throws Exception {
        UUID id = UUID.randomUUID();
        PayloadCompensateUpdate payload = new PayloadCompensateUpdate(EducationControllerTest.class, SagaOutcome.COMPENSATED, "Old Name", null);
        ResponseCompensated response = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(directory.compensateUpdateEducationName(eq(id), any(), any(SagaOutcome.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{id}/compensate-name", id)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(directory).compensateUpdateEducationName(eq(id), any(), any(SagaOutcome.class), anyString());
    }

    @Test
    void shouldCompensateUpdateEducationCategory() throws Exception {
        UUID id = UUID.randomUUID();
        PayloadCompensateUpdate payload = new PayloadCompensateUpdate(EducationControllerTest.class, SagaOutcome.COMPENSATED, "Old Category", null);
        ResponseCompensated response = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(directory.compensateUpdateEducationCategory(eq(id), any(), any(SagaOutcome.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{id}/compensate-category", id)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(directory).compensateUpdateEducationCategory(eq(id), any(), any(SagaOutcome.class), anyString());
    }

    @Test
    void shouldCompensateActivateEducation() throws Exception {
        UUID id = UUID.randomUUID();
        PayloadCompensateUpdate payload = new PayloadCompensateUpdate(EducationControllerTest.class, SagaOutcome.COMPENSATED, null, null);
        ResponseCompensated response = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(directory.compensateActivateEducation(eq(id), any(), any(SagaOutcome.class)))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{id}/compensate-activate", id)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(directory).compensateActivateEducation(eq(id), any(), any(SagaOutcome.class));
    }

    @Test
    void shouldCompensateDeactivateEducation() throws Exception {
        UUID id = UUID.randomUUID();
        PayloadCompensateUpdate payload = new PayloadCompensateUpdate(EducationControllerTest.class, SagaOutcome.COMPENSATED, null, null);
        ResponseCompensated response = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(directory.compensateDeactivateEducation(eq(id), any(), any(SagaOutcome.class)))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{id}/compensate-deactivate", id)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(directory).compensateDeactivateEducation(eq(id), any(), any(SagaOutcome.class));
    }
}