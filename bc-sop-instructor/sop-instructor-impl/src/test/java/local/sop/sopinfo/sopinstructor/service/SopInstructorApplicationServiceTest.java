package local.sop.sopinfo.sopinstructor.service;

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
import local.sop.sopinfo.sopinstructor.application.api.dto.CreateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreatedSopInstructorResult;
import local.sop.sopinfo.sopinstructor.application.api.dto.SopInstructorResponse;
import local.sop.sopinfo.sopinstructor.application.api.dto.ToggleActivateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.service.SopInstructorApplicationService;
import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sopinstructor.domain.ports.out.SopInstructorPort;
import local.sop.sopinfo.sopinstructor.domain.service.SopInstructorDomain;

@ExtendWith(MockitoExtension.class)
class SopInstructorApplicationServiceTest {

    @Mock
    private SopInstructorPort repository;

    @Mock
    private SopInstructorDomain domain;

    @InjectMocks
    private SopInstructorApplicationService service;

    private static final UUID SOP_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, INSTRUCTOR_REF);

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAggregateAndReturnResult() {
        SopInstructor saved = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);

        CreatedSopInstructorResult result = service.create(new CreateSopInstructorCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, result.id());
        verify(repository).save(any());
    }

    @Test
    void create_shouldSetActiveFromCommand() {
        SopInstructor saved = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);
        when(domain.createSopInstructor(VALID_KEY, true)).thenReturn(saved);
        

        CreatedSopInstructorResult result = service.create(new CreateSopInstructorCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, saved.getId());
        verify(repository).save(any());
    }

    // ── toggleActive ───────────────────────────────────────────────────────────

    @Test
    void toggleActive_shouldToggleFromTrueToFalse() {
        SopInstructor existing = SopInstructor.builder()
                .id(VALID_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateSopInstructor(VALID_KEY, true, existing.getCreatedAt().value())).thenReturn(existing);

        SopInstructorResponse result = service.toggleActive(new ToggleActivateSopInstructorCmd(VALID_KEY, true));

        assertFalse(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldToggleFromFalseToTrue() {
        SopInstructor existing = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateSopInstructor(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        SopInstructorResponse result = service.toggleActive(new ToggleActivateSopInstructorCmd(VALID_KEY, false));

        assertTrue(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleActive(new ToggleActivateSopInstructorCmd(VALID_KEY, false)));

        verify(repository, never()).update(any());
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenEntityExists() {
        SopInstructor existing = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        Optional<SopInstructorResponse> result = service.findById(VALID_KEY);

        assertTrue(result.isPresent());
        assertEquals(VALID_KEY, result.get().id());
        assertTrue(result.get().active());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(VALID_KEY));
    }

    // ── getBySopRef ────────────────────────────────────────────────────────────

    @Test
    void getBySopRef_shouldReturnAllMatchingResults() {
        SopInstructor s1 = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findBySopRef(SOP_REF)).thenReturn(List.of(s1));

        List<SopInstructorResponse> result = service.getBySopRef(SOP_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getBySopRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findBySopRef(SOP_REF)).thenReturn(List.of());

        List<SopInstructorResponse> result = service.getBySopRef(SOP_REF);

        assertTrue(result.isEmpty());
    }

    // ── getByEInstructorRef ──────────────────────────────────────────────────────

    @Test
    void getByInstructorRef_shouldReturnAllMatchingResults() {
        SopInstructor s1 = SopInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of(s1));

        List<SopInstructorResponse> result = service.getByInstructorRef(INSTRUCTOR_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByInstructorRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of());

        List<SopInstructorResponse> result = service.getByInstructorRef(INSTRUCTOR_REF);

        assertTrue(result.isEmpty());
    }
}
