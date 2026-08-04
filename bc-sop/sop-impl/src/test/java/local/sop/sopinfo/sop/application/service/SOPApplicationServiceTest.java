package local.sop.sopinfo.sop.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import local.sop.sopinfo.sop.application.api.dto.*;
import local.sop.sopinfo.sop.interfaceadapters.persistence.jpa.SOPEntity;
import local.sop.sopinfo.sop.interfaceadapters.persistence.jpa.SOPSpringDataRepository;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import java.util.Optional;
import java.util.UUID;

class SOPApplicationServiceTest {
    
    @Mock
    private SOPSpringDataRepository repository;
    
    private SOPApplicationService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SOPApplicationService(repository);
    }
    
    @Test
    void findById_shouldReturnSOPResponse() {
        UUID id = UUID.randomUUID();
        SOPQuery query = new SOPQuery(id);
        SOPEntity entity = new SOPEntity(id, "Ringsted Data/IT", "P7-10", "IT Support");
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        
        SopResponse result = service.findById(query).get();
        
        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Ringsted Data/IT", result.name());
        assertEquals("P7-10", result.address());
        assertEquals("IT Support", result.education());
        verify(repository).findById(id);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenQueryIsNull() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(null));
        
        assertEquals("sop.notFound", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUUIDIsNull() {
        SOPQuery query = new SOPQuery(null);
        when(repository.findById(any())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(query));
        
        assertEquals("sop.notFound", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        SOPQuery query = new SOPQuery(id);
        
        when(repository.findById(id)).thenReturn(Optional.empty());
        
        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(query));
        
        assertEquals("sop.notFound", exception.getMessage());
        verify(repository).findById(id);
    }
}