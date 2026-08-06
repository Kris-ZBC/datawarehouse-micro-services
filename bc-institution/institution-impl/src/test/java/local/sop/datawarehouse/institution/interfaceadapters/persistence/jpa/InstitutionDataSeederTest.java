package local.sop.datawarehouse.institution.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
public class InstitutionDataSeederTest {

    @Mock
    private InstitutionSpringDataRepository repository;

    @InjectMocks
    private InstitutionDataSeeder seeder;

    private final UUID zbcId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void run_seedsDataWhenDatabaseIsEmpty() {
        // Arrange
        when(repository.existsById(zbcId)).thenReturn(false);
        ArgumentCaptor<InstitutionEntity> captor = ArgumentCaptor.forClass(InstitutionEntity.class);

        // Act
        seeder.run();

        // Assert
        verify(repository).existsById(zbcId);
        verify(repository).save(captor.capture());
        
        InstitutionEntity savedEntity = captor.getValue();
        assertEquals(zbcId, savedEntity.getId());
        assertEquals("ZBC Ringsted", savedEntity.getName());
        assertEquals("Ahorn Alle 5, Ringsted", savedEntity.getAddress());

        verifyNoMoreInteractions(repository);
    }

    @Test
    void run_doesNotSeedWhenDataAlreadyExists() {
        // Arrange
        when(repository.existsById(zbcId)).thenReturn(true);

        // Act
        seeder.run();

        // Assert
        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).existsById(zbcId);
        inOrder.verify(repository, never()).save(any(InstitutionEntity.class));
        
        verifyNoMoreInteractions(repository);
    }
}
