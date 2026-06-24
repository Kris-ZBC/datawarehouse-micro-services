package local.sop.sopinfo.workhour.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.workhour.application.api.WorkHourDirectory;
import local.sop.sopinfo.workhour.application.api.dto.CreateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleIdQuery;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleParamsQuery;
import local.sop.sopinfo.workhour.application.api.dto.WorkScheduleResponse;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "bc.qualifier=work_hour"

})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InternalWorkHourScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkHourDirectory workHours;

    @Autowired
    private ObjectMapper objectMapper;

    private WorkHourSchedule testSchedule;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testSchedule = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(testId))
            .startTime(WorkScheduleTime.of("07:00"))
            .endTime(WorkScheduleTime.of("16:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(SopRef.of(UUID.randomUUID()))
            .build();
    }

    @Test
    void create_shouldReturn201WithLocationHeader() throws Exception {
        // Arrange
        CreateWorkHourScheduleCmd cmd = new CreateWorkHourScheduleCmd("07:00", "16:00", "MONDAY", UUID.randomUUID());
         when(workHours.create(any(CreateWorkHourScheduleCmd.class))).thenReturn(testId);

        // Act & Assert
        mockMvc.perform(post("/internal/workhours").with(user("registration-saga").roles("INTERNAL"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/internal/workhours/" + testId))
                .andExpect(jsonPath("$.id").value(testId.toString()));
        verify(workHours, times(1)).create(any(CreateWorkHourScheduleCmd.class));
    }

    @Test
    void create_shouldReturn400WhenInvalidTimeFormat() throws Exception {
        // Arrange
        CreateWorkHourScheduleCmd invalidCmd = new CreateWorkHourScheduleCmd("invalid", "16:00", "MONDAY", UUID.randomUUID());
        
        // Mock til at simulere hvad der sker når WorkScheduleTime.of("invalid") kaldes
        when(workHours.create(any(CreateWorkHourScheduleCmd.class)))
            .thenThrow(new ValidationException("time.invalid", Map.of("field", "invalid")));

        // Act & Assert
        mockMvc.perform(post("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCmd)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.key").value("time.invalid"))
                .andExpect(jsonPath("$.args.field").value("invalid"));

        verify(workHours, times(1)).create(any(CreateWorkHourScheduleCmd.class));
    }

    @Test
    void create_shouldReturn500WhenServiceThrowsException() throws Exception {
        // Arrange
        CreateWorkHourScheduleCmd cmd = new CreateWorkHourScheduleCmd("07:00", "16:00", "MONDAY", UUID.randomUUID());
        when(workHours.create(any(CreateWorkHourScheduleCmd.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isInternalServerError());

        verify(workHours, times(1)).create(any(CreateWorkHourScheduleCmd.class));
    }

    @Test
    void readById_shouldReturn200WhenFound() throws Exception {
        // Arrange
        WorkScheduleResponse response = new WorkScheduleResponse(testSchedule.getId().value(), "test", "test", WeekDay.FRIDAY, null);
        when(workHours.readById(any(FindByScheduleIdQuery.class))).thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(get("/internal/workhours/workhour")
                .with(user("registration-saga").roles("INTERNAL"))
                .param("id", testId.toString()))  // ← Brug query parameter i stedet for body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()));

        verify(workHours, times(1)).readById(any(FindByScheduleIdQuery.class));
    }

    @Test
    void readById_shouldReturn204WhenNotFound() throws Exception {
        // Arrange
        when(workHours.readById(any(FindByScheduleIdQuery.class))).thenReturn(Optional.ofNullable(null));

        // Act & Assert
        mockMvc.perform(get("/internal/workhours/workhour")
                .with(user("registration-saga").roles("INTERNAL"))
                .param("id", testId.toString()))  // ← Brug query parameter i stedet for body
                .andExpect(status().isNoContent());

        verify(workHours, times(1)).readById(any(FindByScheduleIdQuery.class));
    }

    @Test
    void delete_shouldReturn204WhenSuccessful() throws Exception {
        // Arrange
        FindByScheduleIdQuery query = new FindByScheduleIdQuery(testId);
        doNothing().when(workHours).delete(any(FindByScheduleIdQuery.class));

        // Act & Assert
        mockMvc.perform(delete("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isNoContent());

        verify(workHours, times(1)).delete(any(FindByScheduleIdQuery.class));
    }

    @Test
    void readBySearchParams_shouldReturn200WhenFound() throws Exception {
        // Arrange
        WorkScheduleResponse response = new WorkScheduleResponse(testSchedule.getId().value(), "test", "test", WeekDay.FRIDAY, null);
        when(workHours.readByParams(any(FindByScheduleParamsQuery.class)))
            .thenReturn(Arrays.asList(response));

        // Act & Assert - Test med alle parametre
        mockMvc.perform(get("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL"))
                .param("id", testId.toString())
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testId.toString()));

        verify(workHours, times(1)).readByParams(any(FindByScheduleParamsQuery.class));
    }
    @Test
    void readBySearchParams_shouldWorkWithOnlyId() throws Exception {
        mockMvc.perform(get("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL"))
                .param("id", testId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void readBySearchParams_shouldWorkWithOnlyDateRange() throws Exception {
        mockMvc.perform(get("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL"))
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    void readBySearchParams_shouldWorkWithNoParameters() throws Exception {
        mockMvc.perform(get("/internal/workhours")
                .with(user("registration-saga").roles("INTERNAL")))
                .andExpect(status().isOk());
    }
}
