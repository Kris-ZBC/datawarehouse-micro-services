package local.sop.sopinfo.apprentice.interfaceweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.sop.sopinfo.apprentice.application.api.ApprenticeDirectory;
import local.sop.sopinfo.apprentice.application.api.dto.ApprenticeResponse;
import local.sop.sopinfo.apprentice.application.api.dto.CreateApprenticeCmd;
import local.sop.sopinfo.apprentice.application.api.dto.CreatedApprenticeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprenticeController.class)
class ApprenticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApprenticeDirectory directory;

    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final UUID EDU_REF = UUID.randomUUID();
    private static final UUID APPRENTICE_ID = UUID.randomUUID();

    @Test
    @WithMockUser
    void createApprentice_shouldReturnCreatedResponse_whenValid() throws Exception {
        // Arrange
        CreateApprenticeCmd cmd = new CreateApprenticeCmd(PERSON_REF, EDU_REF);
        CreatedApprenticeResponse mockResponse = new CreatedApprenticeResponse(APPRENTICE_ID);

        when(directory.createApprentice(any(CreateApprenticeCmd.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/internal/apprentices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apprenticeId").value(APPRENTICE_ID.toString()));

        verify(directory).createApprentice(any(CreateApprenticeCmd.class));
    }

    @Test
    @WithMockUser
    void createApprentice_shouldReturnBadRequest_whenFieldsAreNull() throws Exception {
        // Arrange
        CreateApprenticeCmd invalidCmd = new CreateApprenticeCmd(null, null);

        // Act & Assert
        mockMvc.perform(post("/internal/apprentices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCmd)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(directory);
    }

    @Test
    @WithMockUser
    void findById_shouldReturnApprentice_whenExists() throws Exception {
        // Arrange
        ApprenticeResponse response = new ApprenticeResponse(APPRENTICE_ID, PERSON_REF, EDU_REF);

        when(directory.findById(APPRENTICE_ID)).thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(get("/internal/apprentices/{id}", APPRENTICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apprenticeId").value(APPRENTICE_ID.toString()));
    }

    @Test
    @WithMockUser
    void findById_shouldReturn404_whenNotFound() throws Exception {
        // Arrange
        when(directory.findById(APPRENTICE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/internal/apprentices/{id}", APPRENTICE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void findByEducationLineId_shouldReturnApprentices_whenExists() throws Exception {
        // Arrange
        ApprenticeResponse response = new ApprenticeResponse(APPRENTICE_ID, PERSON_REF, EDU_REF);

        when(directory.findByEducationLineId(EDU_REF)).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/internal/apprentices/by-education-line/{id}", EDU_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].educationLineRef").value(EDU_REF.toString()));
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnListOfApprentices() throws Exception {
        // Arrange
        List<ApprenticeResponse> list = List.of(
                new ApprenticeResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                new ApprenticeResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );

        when(directory.findAll()).thenReturn(list);

        // Act & Assert
        mockMvc.perform(get("/internal/apprentices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}