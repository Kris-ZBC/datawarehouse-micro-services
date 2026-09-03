package local.sop.datawarehouse.instructor.interfaceweb;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.instructor.application.api.InstructorDirectory;
import local.sop.datawarehouse.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.datawarehouse.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.datawarehouse.instructor.application.api.dto.InstructorResponse;

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

    @Test
    void pingShould_returnPing() throws Exception {
        mockMvc.perform(get("/internal/instructors/ping"))
                .andExpect(status().isOk());
    }

    // NEW: coverage for the role-resolution endpoint added for
    // login-saga.
    @Test
    void findByPersonRef_shouldReturn200_whenInstructorExists() throws Exception {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        when(instructorDirectory.findByPersonRef(personRef))
                .thenReturn(java.util.Optional.of(new InstructorResponse(id, personRef)));

        mockMvc.perform(get("/internal/instructors/by-person-ref/{personRef}", personRef))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.personRef").value(personRef.toString()));
    }

    @Test
    void findByPersonRef_shouldReturn204_whenNoInstructorForThatPerson() throws Exception {
        UUID personRef = UUID.randomUUID();

        when(instructorDirectory.findByPersonRef(personRef)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/internal/instructors/by-person-ref/{personRef}", personRef))
                .andExpect(status().isNoContent());
    }

    @Test
    void compensateShouldReturnOk_whenResultIsNotNull() throws Exception{
        UUID id = UUID.randomUUID();

        ResponseCompensated response = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(instructorDirectory.compensate(any(), any(), any())).thenReturn(response);

        String requestBody = """
                {
                  "sagaOutcome": "COMPENSATED"
                }
                """.formatted(UUID.randomUUID());
        mockMvc.perform(put("/internal/instructors/{id}/compensate/create", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
        
        verify(instructorDirectory).compensate(any(), any(), any());
    }

    @Test
    void compensateShouldReturnNoContent_whenResultIsNull() throws Exception {
        UUID id = UUID.randomUUID();

        when(instructorDirectory.compensate(any(), any(), any())).thenReturn(null);

        String requestBody = """
                {
                  "sagaOutcome": "COMPENSATED"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(put("/internal/instructors/{id}/compensate/create", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNoContent());

        verify(instructorDirectory).compensate(any(), any(), any());
    }
}