package local.sop.sopinfo.messageperson.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.sopinfo.messageperson.application.api.dto.CreatedMessagePersonResult;
import local.sop.sopinfo.messageperson.application.api.dto.MessagePersonResponse;
import local.sop.sopinfo.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;
import local.sop.sopinfo.messageperson.application.service.MessagePersonApplicationService;
import local.sop.sopinfo.messageperson.domain.model.MessagePerson;
import local.sop.sopinfo.messageperson.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.messageperson.domain.ports.out.MessagePersonPort;
import local.sop.sopinfo.messageperson.domain.service.MessagePersonDomain;

@ExtendWith(MockitoExtension.class)
class MessagePersonApplicationServiceTest {

    @Mock
    private MessagePersonPort repository;

    @Mock
    private MessagePersonDomain domain;

    @InjectMocks
    private MessagePersonApplicationService service;

    private static final UUID MESSAGE_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(MESSAGE_REF, PERSON_REF);

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAggregateAndReturnResult() {
        MessagePerson saved = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);

        CreatedMessagePersonResult result = service.create(new CreateMessagePersonCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, result.id());
        verify(repository).save(any());
    }

    @Test
    void create_shouldSetActiveFromCommand() {
        MessagePerson saved = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);
        when(domain.createMessagePerson(VALID_KEY, true)).thenReturn(saved);
        

        CreatedMessagePersonResult result = service.create(new CreateMessagePersonCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, saved.getId());
        verify(repository).save(any());
    }

    // ── toggleActive ───────────────────────────────────────────────────────────

    @Test
    void toggleActive_shouldToggleFromTrueToFalse() {
        MessagePerson existing = MessagePerson.builder()
                .id(VALID_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateMessagePerson(VALID_KEY, true, existing.getCreatedAt().value())).thenReturn(existing);

        MessagePersonResponse result = service.toggleActive(new ToggleActivateMessagePersonCmd(VALID_KEY, true));

        assertFalse(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldToggleFromFalseToTrue() {
        MessagePerson existing = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateMessagePerson(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        MessagePersonResponse result = service.toggleActive(new ToggleActivateMessagePersonCmd(VALID_KEY, false));

        assertTrue(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleActive(new ToggleActivateMessagePersonCmd(VALID_KEY, false)));

        verify(repository, never()).update(any());
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenEntityExists() {
        MessagePerson existing = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        Optional<MessagePersonResponse> result = service.findById(VALID_KEY);

        assertTrue(result.isPresent());
        assertEquals(VALID_KEY, result.get().id());
        assertTrue(result.get().active());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(VALID_KEY));
    }

    // ── getByMessageRef ────────────────────────────────────────────────────────────

    @Test
    void getByMessageRef_shouldReturnAllMatchingResults() {
        MessagePerson s1 = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByMessageRef(MESSAGE_REF)).thenReturn(List.of(s1));

        List<MessagePersonResponse> result = service.getByMessageRef(MESSAGE_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByMessageRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByMessageRef(MESSAGE_REF)).thenReturn(List.of());

        List<MessagePersonResponse> result = service.getByMessageRef(MESSAGE_REF);

        assertTrue(result.isEmpty());
    }

    // ── getByEPersonRef ──────────────────────────────────────────────────────

    @Test
    void getByPersonRef_shouldReturnAllMatchingResults() {
        MessagePerson s1 = MessagePerson.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByPersonRef(PERSON_REF)).thenReturn(List.of(s1));

        List<MessagePersonResponse> result = service.getByPersonRef(PERSON_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByPersonRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByPersonRef(PERSON_REF)).thenReturn(List.of());

        List<MessagePersonResponse> result = service.getByPersonRef(PERSON_REF);

        assertTrue(result.isEmpty());
    }
}
