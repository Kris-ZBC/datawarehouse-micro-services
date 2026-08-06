package local.sop.sopinfo.personnotification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.datawarehouse.personnotification.application.api.dto.CreatePersonNotificationCmd;
import local.sop.datawarehouse.personnotification.application.api.dto.CreatedPersonNotificationResult;
import local.sop.datawarehouse.personnotification.application.api.dto.PersonNotificationResponse;
import local.sop.datawarehouse.personnotification.application.api.dto.ToggleActivatePersonNotificationCmd;
import local.sop.datawarehouse.personnotification.application.service.PersonNotificationApplicationService;
import local.sop.datawarehouse.personnotification.domain.model.PersonNotification;
import local.sop.datawarehouse.personnotification.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.personnotification.domain.ports.out.PersonNotificationPort;
import local.sop.datawarehouse.personnotification.domain.service.PersonNotificationDomain;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PersonNotificationApplicationServiceTest {

    @Mock
    private PersonNotificationPort repository;

    @Mock
    private PersonNotificationDomain domain;

    @InjectMocks
    private PersonNotificationApplicationService service;

    private static final UUID NOTIFICATION_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final Boolean ACTIVE = true;
    private static final CompositeKey VALID_KEY = new CompositeKey(NOTIFICATION_REF, PERSON_REF);

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAggregateAndReturnResult() {
        PersonNotification saved = PersonNotification.builder()
                .id(VALID_KEY)
                .active(ACTIVE)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);

        CreatedPersonNotificationResult result = service.create(new CreatePersonNotificationCmd(VALID_KEY, ACTIVE));

        assertNotNull(result);
        assertEquals(VALID_KEY, result.id());
        verify(repository).save(any());
    }

    
    @Test
    void findById_shouldReturnResponse_whenEntityExists() {
        PersonNotification existing = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        Optional<PersonNotificationResponse> result = service.findById(VALID_KEY);

        assertTrue(result.isPresent());
        assertEquals(VALID_KEY, result.get().id());
        assertTrue(result.get().active());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(VALID_KEY));
    }
    // ── toggleActive ───────────────────────────────────────────────────────────

    @Test
    void toggleActive_shouldToggleFromTrueToFalse() {
        PersonNotification existing = PersonNotification.builder()
                .id(VALID_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivatePersonNotification(VALID_KEY, true, existing.getCreatedAt().value())).thenReturn(existing);

        PersonNotificationResponse result = service.toggleActive(new ToggleActivatePersonNotificationCmd(VALID_KEY, true));

        assertFalse(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldToggleFromFalseToTrue() {
        PersonNotification existing = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivatePersonNotification(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        PersonNotificationResponse result = service.toggleActive(new ToggleActivatePersonNotificationCmd(VALID_KEY, false));

        assertTrue(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleActive(new ToggleActivatePersonNotificationCmd(VALID_KEY, false)));

        verify(repository, never()).update(any());
    }


    // ── getByNotificationRef ────────────────────────────────────────────────────────────

    @Test
    void getByNotificationRef_shouldReturnAllMatchingResults() {
        PersonNotification s1 = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByNotificationRef(NOTIFICATION_REF)).thenReturn(List.of(s1));

        List<PersonNotificationResponse> result = service.getByNotificationRef(NOTIFICATION_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByNotificationRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByNotificationRef(NOTIFICATION_REF)).thenReturn(List.of());

        List<PersonNotificationResponse> result = service.getByNotificationRef(NOTIFICATION_REF);

        assertTrue(result.isEmpty());
    }

    // ── getByPersonRef ──────────────────────────────────────────────────────

    @Test
    void getByPersonRef_shouldReturnAllMatchingResults() {
        PersonNotification p = PersonNotification.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByPersonRef(PERSON_REF)).thenReturn(List.of(p));

        List<PersonNotificationResponse> result = service.getByPersonRef(PERSON_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByPersonRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByPersonRef(PERSON_REF)).thenReturn(List.of());

        List<PersonNotificationResponse> result = service.getByPersonRef(PERSON_REF);

        assertTrue(result.isEmpty());
    }


}
