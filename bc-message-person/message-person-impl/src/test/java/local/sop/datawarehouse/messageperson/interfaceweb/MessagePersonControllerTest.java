package local.sop.datawarehouse.messageperson.interfaceweb;

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
import local.sop.datawarehouse.messageperson.application.api.MessagePersonDirectory;
import local.sop.datawarehouse.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.datawarehouse.messageperson.application.api.dto.CreatedMessagePersonResult;
import local.sop.datawarehouse.messageperson.application.api.dto.MessagePersonResponse;
import local.sop.datawarehouse.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;

@WebMvcTest(MessagePersonController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MessagePersonControllerTest {

    private static final String URL = "/internal/message-persons";

    private static final UUID MESSAGE_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(MESSAGE_REF, PERSON_REF);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessagePersonDirectory directory;

    // ── POST /internal/message-persons ─────────────────────────────────────────

    @Test
    void create_shouldReturn201_withLocationHeader_whenCommandIsValid() throws Exception {
        CreatedMessagePersonResult result = new CreatedMessagePersonResult(VALID_KEY);

        when(directory.create(any(CreateMessagePersonCmd.class))).thenReturn(result);

        CreateMessagePersonCmd cmd = new CreateMessagePersonCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/internal/message-persons/message-ref/" + MESSAGE_REF + "/person-ref/" + PERSON_REF))
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
        when(directory.create(any(CreateMessagePersonCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        CreateMessagePersonCmd cmd = new CreateMessagePersonCmd(VALID_KEY, true);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /internal/message-persons/toggle-active ────────────────────────────

    @Test
    void toggleActive_shouldReturn200_withUpdatedResponse_whenCommandIsValid() throws Exception {
        MessagePersonResponse response = new MessagePersonResponse(
                VALID_KEY, false, LocalDateTime.now().minusDays(1));

        when(directory.toggleActive(any(ToggleActivateMessagePersonCmd.class))).thenReturn(response);

        ToggleActivateMessagePersonCmd cmd = new ToggleActivateMessagePersonCmd(VALID_KEY, false);

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
        when(directory.toggleActive(any(ToggleActivateMessagePersonCmd.class)))
                .thenThrow(new NotFoundException("key.notfound",
                        Map.of("field", "id", "value", VALID_KEY)));

        ToggleActivateMessagePersonCmd cmd = new ToggleActivateMessagePersonCmd(VALID_KEY, false);

        mockMvc.perform(put(URL + "/toggle-active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());
    }

    // ── GET /internal/message-persons/message-ref/{messageId}/person-ref/{personId}

    @Test
    void findById_shouldReturn200_withResponse_whenEntityExists() throws Exception {
        MessagePersonResponse response = new MessagePersonResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.of(response));

        mockMvc.perform(get(URL + "/message-ref/{messageId}/person-ref/{personId}",
                        MESSAGE_REF, PERSON_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void findById_shouldReturn404_whenEntityDoesNotExist() throws Exception {
        when(directory.findById(any(CompositeKey.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/message-ref/{messageId}/person-ref/{personId}",
                        MESSAGE_REF, PERSON_REF))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn400_whenMessageIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/message-ref/{messageId}/person-ref/{personId}",
                        "not-a-uuid", PERSON_REF))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturn400_whenPersonIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/message-ref/{messageId}/person-ref/{personId}",
                        MESSAGE_REF, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/message-persons/by-message/{messageId} ───────────────────────────

    @Test
    void findByMessageId_shouldReturn200_withList_whenResultsExist() throws Exception {
        MessagePersonResponse response = new MessagePersonResponse(
                VALID_KEY, true, LocalDateTime.now().minusDays(1));

        when(directory.getByMessageRef(MESSAGE_REF)).thenReturn(List.of(response));

        mockMvc.perform(get(URL + "/by-message/{messageId}", MESSAGE_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void findByMessageId_shouldReturn200_withEmptyList_whenNoResultsExist() throws Exception {
        when(directory.getByMessageRef(MESSAGE_REF)).thenReturn(List.of());

        mockMvc.perform(get(URL + "/by-message/{messageId}", MESSAGE_REF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByMessageId_shouldReturn400_whenMessageIdIsNotValidUUID() throws Exception {
        mockMvc.perform(get(URL + "/by-message/{messageId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /internal/message-persons/by-person/{personId} ──────────────

    @Test
    void findByPersonId_shouldReturn200_withList_whenResultsExist() throws Exception {
        MessagePersonResponse response = new MessagePersonResponse(
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
