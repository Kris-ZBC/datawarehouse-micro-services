package local.sop.sopinfo.institution.application.service;

import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.institution.application.api.dto.InstitutionQuery;
import local.sop.sopinfo.institution.application.api.dto.InstitutionResponse;
import local.sop.sopinfo.institution.interfaceadapters.persistence.jpa.InstitutionEntity;
import local.sop.sopinfo.institution.interfaceadapters.persistence.jpa.InstitutionSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstitutionApplicationServiceTest {

    private InstitutionSpringDataRepository repository;
    private InstitutionApplicationService service;

    @BeforeEach
    void setUp() {
        repository = mock(InstitutionSpringDataRepository.class);
        service = new InstitutionApplicationService(repository);
    }

    @Test
    void findById_throwsNotFoundException_whenEntityDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
            service.findById(new InstitutionQuery(id))
        );

        verify(repository).findById(id);
    }

    @Test
    void findById_returnsResponse_whenEntityExists() {
        UUID id = UUID.randomUUID();
        InstitutionEntity entity = new InstitutionEntity(
                id,
                "ZBC Ringsted",
                "Ahorn Alle 5, Ringsted"
        );

        when(repository.findById(id)).thenReturn(Optional.of(entity));

        InstitutionResponse result = service.findById(new InstitutionQuery(id));

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("ZBC Ringsted", result.name());
        assertEquals("Ahorn Alle 5, Ringsted", result.address());

        verify(repository).findById(id);
    }
}