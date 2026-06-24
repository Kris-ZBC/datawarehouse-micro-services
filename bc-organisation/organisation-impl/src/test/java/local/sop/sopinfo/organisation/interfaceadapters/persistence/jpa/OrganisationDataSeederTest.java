package local.sop.sopinfo.organisation.interfaceadapters.persistence.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class OrganisationDataSeederTest {
    private static final UUID ZBC_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    OrganisationSpringDataRepository repository;
    OrganisationDataSeeder seeder;

	@BeforeEach
	void setup() {
        repository = org.mockito.Mockito.mock(OrganisationSpringDataRepository.class);
        seeder = new OrganisationDataSeeder(repository);
    }

	@Test
	void run_whenOrganisationAlreadyExists_doesNotSave() throws Exception {
		Mockito.when(repository.existsById(ZBC_ID)).thenReturn(true);
		seeder.run();
        verify(repository, times(1)).existsById(ZBC_ID);
        verify(repository, never()).save(any());
        verifyNoMoreInteractions(repository);
	}

	@Test
	void run_whenOrganisationDoesNotExist_savesZBC() throws Exception {
		Mockito.when(repository.existsById(ZBC_ID)).thenReturn(false);
		seeder.run();
		verify(repository, times(1)).existsById(ZBC_ID);
		verify(repository, times(1)).save(any(OrganisationEntity.class));
		verifyNoMoreInteractions(repository);
	}
}
