package local.sop.sopinfo.instructor.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import local.sop.sopinfo.instructor.application.api.InstructorDirectory;
import local.sop.sopinfo.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.sopinfo.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.sopinfo.instructor.application.api.dto.InstructorResponse;

@WebMvcTest(controllers = InternalInstructorController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalInstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstructorDirectory instructorDirectory;

    @Test
    void getAllInstructors_shouldReturnAllInstructors() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID personRef1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();

        List<InstructorResponse> responses = List.of(
                new InstructorResponse(id1, personRef1),
                new InstructorResponse(id2, personRef2)
        );

        when(instructorDirectory.findAll()).thenReturn(responses);

        mockMvc.perform(get("/internal/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].personRef").value(personRef1.toString()))
                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].personRef").value(personRef2.toString()));

        verify(instructorDirectory).findAll();
    }

    @Test
    void findInstructorId_shouldReturnInstructorById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        InstructorResponse response = new InstructorResponse(id, personRef);

        when(instructorDirectory.findById(id)).thenReturn(response);

        mockMvc.perform(get("/internal/instructors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.personRef").value(personRef.toString()));

        verify(instructorDirectory).findById(id);
    }

    @Test
    void createInstructor_shouldReturnCreatedInstructorResponse() throws Exception {
        UUID createdId = UUID.randomUUID();

        when(instructorDirectory.createInstructor(any(CreateInstructorCmd.class)))
                .thenReturn(new CreatedInstructorResponse(createdId));

        String requestBody = """
                {
                  "personRef": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/internal/instructors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()));

        verify(instructorDirectory).createInstructor(any(CreateInstructorCmd.class));
    }
}