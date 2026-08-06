package local.sop.datawarehouse.apprentice.application.service;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.apprentice.application.api.dto.ApprenticeResponse;
import local.sop.datawarehouse.apprentice.application.api.dto.CreateApprenticeCmd;
import local.sop.datawarehouse.apprentice.application.api.dto.CreatedApprenticeResponse;
import local.sop.datawarehouse.apprentice.domain.model.Apprentice;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.ApprenticeId;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.EducationLineRef;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.apprentice.domain.ports.out.ApprenticeRepositoryPort;
import local.sop.datawarehouse.apprentice.domain.service.ApprenticeDomain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "bc.qualifier=apprentice"
})

@ExtendWith(MockitoExtension.class)
class ApprenticeApplicationServiceTest {

    @Mock
    private ApprenticeRepositoryPort repository;

    @Mock
    private ApprenticeDomain domain;

    @InjectMocks
    private ApprenticeApplicationService service;

    private final UUID apprenticeUuid = UUID.randomUUID();
    private final UUID personUuid = UUID.randomUUID();
    private final UUID educationUuid = UUID.randomUUID();


    // --- Create Tests ---

    @Test
    void createApprentice_shouldReturnCreatedResponse() {
        CreateApprenticeCmd cmd = new CreateApprenticeCmd(personUuid, educationUuid);
        Apprentice mockApprentice = createMockApprentice();
        
        when(domain.create(any(), any(), any())).thenReturn(mockApprentice);
        when(repository.save(any(Apprentice.class))).thenReturn(mockApprentice);

        CreatedApprenticeResponse response = service.createApprentice(cmd);

        assertThat(response.apprenticeId()).isEqualTo(apprenticeUuid);
    }


// ... inside the test class ...

    @Test
    @DisplayName("Should propagate RuntimeException")
    void createApprentice_shouldPropagateRuntimeException() {
        CreateApprenticeCmd cmd = new CreateApprenticeCmd(personUuid, educationUuid);

        when(domain.create(any(), any(), any()))
                .thenThrow(new RuntimeException("Database down"));

        assertThatThrownBy(() -> service.createApprentice(cmd))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database down");
    }

    

    @Test
    @DisplayName("Should wrap ValidationException into application ValidationException")
    void createApprentice_shouldWrapValidationException() {
        CreateApprenticeCmd cmd = new CreateApprenticeCmd(personUuid, educationUuid);

        ValidationException domainEx =
                new ValidationException("invalid.data", Collections.emptyMap());

        when(domain.create(any(), any(), any())).thenThrow(domainEx);

        assertThatThrownBy(() -> service.createApprentice(cmd))
                .isExactlyInstanceOf(ValidationException.class)
                .hasMessageContaining("apprentice.create.failed");
    }

    // --- findById Tests ---

    @Test
    void findById_whenFound_shouldReturnResponse() {
        Apprentice mockApprentice = createMockApprentice();
        when(repository.findById(apprenticeUuid)).thenReturn(Optional.of(mockApprentice));

        Optional<ApprenticeResponse> result = service.findById(apprenticeUuid);

        assertThat(result).isPresent();
        assertThat(result.get().apprenticeId()).isEqualTo(apprenticeUuid);
    }

    @Test
    void findById_whenNotFound_shouldReturnEmpty() {
        when(repository.findById(apprenticeUuid)).thenReturn(Optional.empty());
        Optional<ApprenticeResponse> result = service.findById(apprenticeUuid);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById should throw exception when ID is null")
    void findById_shouldThrowExceptionOnNull() {
        // The service itself uses Map.of(), so we just check for the expected type and key
        assertThatThrownBy(() -> service.findById(null))
                .isInstanceOf(ValidationException.class)
                .matches(ex -> ((ValidationException) ex).getMessage().contains("id.required"));
    }

    // --- findByEducationLineId Tests ---

    @Test
    void findByEducationLineId_whenFound_shouldReturnResponse() {
        Apprentice mockApprentice = createMockApprentice();
        when(repository.findByEducationLineId(educationUuid)).thenReturn(List.of(mockApprentice));

        List<ApprenticeResponse> result = service.findByEducationLineId(educationUuid);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).educationLineRef()).isEqualTo(educationUuid);
    }

    @Test
    @DisplayName("findByEducationLineId should throw exception when ID is null")
    void findByEducationLineId_shouldThrowExceptionOnNull() {
        assertThatThrownBy(() -> service.findByEducationLineId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("education_line_id.required");
    }

    // --- findAll Tests ---

    @Test
    void findAll_shouldReturnList() {
        Apprentice mockApprentice = createMockApprentice();
        when(repository.findAll()).thenReturn(List.of(mockApprentice));

        List<ApprenticeResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).apprenticeId()).isEqualTo(apprenticeUuid);
    }

    private Apprentice createMockApprentice() {
        return Apprentice.builder()
                .id(new ApprenticeId(apprenticeUuid))
                .personRef(new PersonRef(personUuid))
                .educationLineRef(new EducationLineRef(educationUuid))
                .build();
    }
    
    
    
}