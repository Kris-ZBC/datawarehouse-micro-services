package local.sop.sopinfo.organisation.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.organisation.application.api.OrganisationDirectory;
import local.sop.sopinfo.organisation.application.api.dto.OrganisationQuery;
import local.sop.sopinfo.organisation.interfaceadapters.persistence.jpa.OrganisationEntity;
import local.sop.sopinfo.organisation.interfaceadapters.persistence.jpa.OrganisationSpringDataRepository;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;

public class OrganisationApplicationServiceTest {

    OrganisationSpringDataRepository repository;
    OrganisationDirectory service;

    @BeforeEach
    void setup(){
        repository = org.mockito.Mockito.mock(OrganisationSpringDataRepository.class);
        service = new OrganisationApplicationService(repository);

    }

    @Test 
    void happyPath_findWithValidId_willSucceed(){
        var id = java.util.UUID.randomUUID();
        var query = new OrganisationQuery (id);
        var response = new OrganisationEntity(id, "Test Organisation", "12 34 56 78");
        Mockito.when(repository.findById(query.id())).thenReturn((java.util.Optional.of(response)));
        var result = service.findById(id);
        assert(result.isPresent());
        assert(result.get().id().equals(id));
        assert(result.get().name().equals("Test Organisation"));
        assert(result.get().cvr().equals("12 34 56 78"));
        verify(repository, Mockito.times(1)).findById(query.id());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findWithInvalidId_willThrowNotFound(){
        var id = java.util.UUID.randomUUID();
        var query = new OrganisationQuery (id);
        Mockito.when(repository.findById(query.id())).thenReturn((java.util.Optional.empty()));
        assertThrows(NotFoundException.class, () -> service.findById(id));
        verify(repository, Mockito.times(1)).findById(query.id());
        verifyNoMoreInteractions(repository);
    }

	@Test
	void findWithInvalidId_willThrowValidationException(){
		var id = java.util.UUID.randomUUID();
		Mockito.when(repository.findById(id)).thenThrow(new RuntimeException("Validation failed"));
		assertThrows(ValidationException.class, () -> service.findById(id));
		verify(repository, Mockito.times(1)).findById(id);
		verifyNoMoreInteractions(repository);
	}
}
