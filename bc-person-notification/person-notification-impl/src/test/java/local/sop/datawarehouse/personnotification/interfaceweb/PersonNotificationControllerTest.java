package local.sop.datawarehouse.personnotification.interfaceweb;

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
import local.sop.datawarehouse.personnotification.application.api.PersonNotificationDirectory;
import local.sop.datawarehouse.personnotification.application.api.dto.CreatePersonNotificationCmd;
import local.sop.datawarehouse.personnotification.application.api.dto.CreatedPersonNotificationResult;
import local.sop.datawarehouse.personnotification.application.api.dto.PersonNotificationResponse;
import local.sop.datawarehouse.personnotification.application.api.dto.ToggleActivatePersonNotificationCmd;

@WebMvcTest(PersonNotificationController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PersonNotificationControllerTest {

    private static final String URL = "/internal/person-notifications";

    private static final UUID NOTIFICATION_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(PERSON_REF,NOTIFICATION_REF);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonNotificationDirectory directory;

    // ── POST /internal/person-notifications ─────────────────────────────────────────

    @Test
    void create_shouldReturn201_withLocationHeader_whenCommandIsValid() throws Exception {
        CreatedPersonNotificationResult result = new CreatedPersonNotificationResult(VALID_KEY);

        when(directory.create(any(CreatePersonNotificationCmd.class))).thenReturn(result);

        CreatePersonNotificationCmd cmd = new CreatePersonNotificationCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/internal/person-notifications/person-ref/" + PERSON_REF + "/notification-ref/" + NOTIFICATION_REF))
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
        when(directory.create(any(CreatePersonNotificationCmd.class)))
                .thenThrow(new NotFoundException("key.not.found",
                        Map.of("field", "id", "value", VALID_KEY)));

        CreatePersonNotificationCmd cmd = new CreatePersonNotificationCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /internal/person-notifications/toggle-active ────────────────────────────

    @Test
    void toggleActive_shouldReturn200_withUpdatedResponse_whenCommandIsValid() throws Exception {
        PersonNotificationResponse response = new PersonNotificationResponse(
                VALID_KEY, false, LocalDateTime.now().minusDays(1));

        when(directory.toggleActive(any(ToggleActivatePersonNotificationCmd.class))).thenReturn(response);

        ToggleActivatePersonNotificationCmd cmd = new ToggleActivatePersonNotificationCmd(VALID_KEY, false);

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
        when(directory.toggleActive(any(ToggleActivatePersonNotificationCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        ToggleActivatePersonNotificationCmd cmd = new ToggleActivatePersonNotificationCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── GET /internal/person-notifications/message-ref/{messageId}/person-ref/{personId}

    @Test
    void findById_shouldReturn200_withResponse_whenEntityExists() throws Exception {
        PersonNotificationResponse response = new PersonNotificationResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.of(response));

        mockMvc.perform(get(URL + "/person-ref/{personId}/notification-ref/{notificationId}",
                        PERSON_REF, NOTIFICATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void findById_shouldReturn404_whenEntityDoesNotExist() throws Exception {
        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/person-ref/{personId}/notification-ref/{notificationId}",
                        PERSON_REF, NOTIFICATION_REF))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn400_whenNotificationIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/person-ref/{personId}/notification-ref/{notificationId}",
                        "not-a-uuid", NOTIFICATION_REF))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturn400_whenPersonIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/person-ref/{personId}/notification-ref/{notificationId}",
                        "not-a-uuid", NOTIFICATION_REF))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/person-notifications/by-message/{messageId} ───────────────────────────

    @Test
    void findByNotificationId_shouldReturn200_withList_whenResultsExist() throws Exception {
        PersonNotificationResponse response = new PersonNotificationResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByNotificationRef(NOTIFICATION_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/by-notification/{notificationId}", NOTIFICATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void findByNotificationId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByNotificationRef(NOTIFICATION_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/by-notification/{notificationId}", NOTIFICATION_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByNotificationId_shouldReturn400_whenNotificationIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/by-notification/{notificationId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/person-notifications/by-person/{personId} ──────────────

    @Test
    void findByPersonId_shouldReturn200_withList_whenResultsExist() throws Exception {
        PersonNotificationResponse response = new PersonNotificationResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByPersonRef(PERSON_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/by-person/{personId}", PERSON_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void findByPersonId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByPersonRef(PERSON_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/by-person/{personId}", PERSON_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByPersonId_shouldReturn400_whenPersonIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/by-person/{personId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

}
