package local.sop.datawarehouse.sopeducation.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.datawarehouse.sopeducation.application.api.dto.CreateSopEducationCmd;
import local.sop.datawarehouse.sopeducation.application.api.dto.CreatedSopEducationResult;
import local.sop.datawarehouse.sopeducation.application.api.dto.SopEducationResponse;
import local.sop.datawarehouse.sopeducation.application.api.dto.ToggleActivateSopEducationCmd;
import local.sop.datawarehouse.sopeducation.domain.model.SopEducation;
import local.sop.datawarehouse.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.sopeducation.domain.ports.out.SopEducationPort;
import local.sop.datawarehouse.sopeducation.domain.service.SopEducationDomain;

@ExtendWith(MockitoExtension.class)
class SopEducationApplicationServiceTest {

    @Mock
    private SopEducationPort repository;

    @Mock
    private SopEducationDomain domain;

    @InjectMocks
    private SopEducationApplicationService service;

    private static final UUID SOP_REF       = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF  = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final CompositeKey VALID_KEY = new CompositeKey(SOP_REF, EDUCATION_REF);

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAggregateAndReturnResult() {
        SopEducation saved = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);
        when(domain.createSopEducation(VALID_KEY, true)).thenReturn(saved);

        CreatedSopEducationResult result = service.create(new CreateSopEducationCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, result.id());
        verify(repository).save(any());
    }

    @Test
    void create_shouldSetActiveFromCommand() {
        SopEducation saved = SopEducation.builder()
                .id(VALID_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);
        when(domain.createSopEducation(VALID_KEY, true)).thenReturn(saved);

        service.create(new CreateSopEducationCmd(VALID_KEY, false));

        verify(repository).save(argThat(s -> !s.isActive()));
    }

    // ── toggleActive ───────────────────────────────────────────────────────────

    @Test
    void toggleActive_shouldToggleFromTrueToFalse() {
        SopEducation existing = SopEducation.builder()
                .id(VALID_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateSopEducation(VALID_KEY, true, existing.getCreatedAt().value())).thenReturn(existing);

        SopEducationResponse result = service.toggleActive(new ToggleActivateSopEducationCmd(VALID_KEY, true));

        assertFalse(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldToggleFromFalseToTrue() {
        SopEducation existing = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
         when(domain.toggleActivateSopEducation(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        SopEducationResponse result = service.toggleActive(new ToggleActivateSopEducationCmd(VALID_KEY, false));

        assertTrue(result.active());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleActive(new ToggleActivateSopEducationCmd(VALID_KEY, false)));

        verify(repository, never()).update(any());
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenEntityExists() {
        SopEducation existing = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        Optional<SopEducationResponse> result = service.findById(VALID_KEY);

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
        SopEducation s1 = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findBySopRef(SOP_REF)).thenReturn(List.of(s1));

        List<SopEducationResponse> result = service.getBySopRef(SOP_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getBySopRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findBySopRef(SOP_REF)).thenReturn(List.of());

        List<SopEducationResponse> result = service.getBySopRef(SOP_REF);

        assertTrue(result.isEmpty());
    }

    // ── getByEducationRef ──────────────────────────────────────────────────────

    @Test
    void getByEducationRef_shouldReturnAllMatchingResults() {
        SopEducation s1 = SopEducation.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByEducationRef(EDUCATION_REF)).thenReturn(List.of(s1));

        List<SopEducationResponse> result = service.getByEducationRef(EDUCATION_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByEducationRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByEducationRef(EDUCATION_REF)).thenReturn(List.of());

        List<SopEducationResponse> result = service.getByEducationRef(EDUCATION_REF);

        assertTrue(result.isEmpty());
    }
}