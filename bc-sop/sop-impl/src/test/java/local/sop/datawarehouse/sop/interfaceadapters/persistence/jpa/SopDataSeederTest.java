package local.sop.datawarehouse.sop.interfaceadapters.persistence.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SopDataSeederTest {

    @Mock
    private SOPSpringDataRepository repository;

    @InjectMocks
    private SopDataSeeder seeder;

    private static final UUID ZBC_SOP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void run_shouldSkip_whenSeedAlreadyPresent() throws Exception {
        when(repository.existsById(ZBC_SOP_ID)).thenReturn(true);

        seeder.run();

        verify(repository, never()).save(any());
    }

    @Test
    void run_shouldSeedSop_whenNotPresent() throws Exception {
        when(repository.existsById(ZBC_SOP_ID)).thenReturn(false);

        seeder.run();

        verify(repository).save(any(SOPEntity.class));
    }
}
