package local.sop.datawarehouse.workhour.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
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
import local.sop.datawarehouse.workhour.application.api.WorkHourDirectory;
import local.sop.datawarehouse.workhour.application.api.dto.CreateWorkHourCmd;
import local.sop.datawarehouse.workhour.application.api.dto.CreatedWorkHourResult;
import local.sop.datawarehouse.workhour.application.api.dto.UpdateWorkHourCmd;
import local.sop.datawarehouse.workhour.application.api.dto.WorkHourResponse;

@WebMvcTest(WorkHourController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkHourControllerTest {

@Autowired
private MockMvc mockMvc;

@Autowired
private ObjectMapper objectMapper;

@MockitoBean
private WorkHourDirectory directory;

// ── create ───────────────────────────────────────────────────────────────

@Test
void create_shouldReturn201_whenCommandIsValid() throws Exception {

    UUID id = UUID.randomUUID();

    when(directory.create(any(CreateWorkHourCmd.class)))
            .thenReturn(new CreatedWorkHourResult(id));

    CreateWorkHourCmd cmd = new CreateWorkHourCmd(LocalTime.of(8, 0), LocalTime.of(16, 0));

    mockMvc.perform(post("/internal/workhours")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cmd)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location","/internal/workhours/" + id))
        .andExpect(jsonPath("$.id").value(id.toString()));
}

@Test
void create_shouldReturn400_whenBodyIsInvalid() throws Exception {

    mockMvc.perform(post("/internal/workhours")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest());
}

// ── update ───────────────────────────────────────────────────────────────

@Test
void update_shouldReturn200_whenCommandIsValid() throws Exception {

    UUID id = UUID.randomUUID();

    WorkHourResponse response = new WorkHourResponse(id,LocalTime.of(8, 0),LocalTime.of(16, 0));

    when(directory.update(any(UpdateWorkHourCmd.class))).thenReturn(response);

    UpdateWorkHourCmd cmd = new UpdateWorkHourCmd(id,LocalTime.of(8, 0),LocalTime.of(16, 0));

    mockMvc.perform(put("/internal/workhours")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cmd)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.startTime").value("08:00:00"))
        .andExpect(jsonPath("$.endTime").value("16:00:00"));
}

@Test
void update_shouldReturn400_whenBodyIsInvalid() throws Exception {

    mockMvc.perform(put("/internal/workhours")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest());
}

// ── findById ─────────────────────────────────────────────────────────────

@Test
void findById_shouldReturn200_whenWorkHourExists() throws Exception {

    UUID id = UUID.randomUUID();

    when(directory.findById(id))
            .thenReturn(Optional.of(new WorkHourResponse(id,LocalTime.of(8, 0),LocalTime.of(16, 0))));

    mockMvc.perform(get("/internal/workhours/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
}

@Test
void findById_shouldReturn404_whenWorkHourDoesNotExist() throws Exception {

    UUID id = UUID.randomUUID();

    when(directory.findById(id))
            .thenReturn(Optional.empty());

    mockMvc.perform(get("/internal/workhours/{id}", id))
            .andExpect(status().isNotFound());
}

// ── findAll ──────────────────────────────────────────────────────────────

@Test
void findAll_shouldReturnList() throws Exception {

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    when(directory.getAll()).thenReturn(List.of(
        new WorkHourResponse(id1,LocalTime.of(8, 0),LocalTime.of(16, 0)),
        new WorkHourResponse(id2,LocalTime.of(9, 0),LocalTime.of(17, 0))));

    mockMvc.perform(get("/internal/workhours"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(id1.toString()))
        .andExpect(jsonPath("$[1].id").value(id2.toString()));
}

@Test
void findAll_shouldReturnEmptyList() throws Exception {

    when(directory.getAll()).thenReturn(List.of());

    mockMvc.perform(get("/internal/workhours"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
}

// ── ping ─────────────────────────────────────────────────────────────────

@Test
void ping_shouldReturnPong() throws Exception {

    mockMvc.perform(get("/internal/workhours/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("PONG!"));
}

}
