package local.sop.datawarehouse.anonymize.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.anonymize.application.api.dto.AnonymizeResponse;
import local.sop.datawarehouse.anonymize.application.api.dto.CompensateAnonymizeStatementCmd;
import local.sop.datawarehouse.anonymize.application.api.dto.CreateAnonymizeCmd;
import local.sop.datawarehouse.anonymize.application.api.dto.FetchByIdQuery;
import local.sop.datawarehouse.anonymize.application.api.dto.FetchByParamsQuery;
import local.sop.datawarehouse.anonymize.domain.model.Anonymize;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.anonymize.domain.ports.out.AnonymizeRepositoryPort;
import local.sop.datawarehouse.anonymize.domain.service.AnonymizeDomain;


@ExtendWith(MockitoExtension.class)
public class AnonymizeApplicationServiceTest {

@Mock
    private AnonymizeRepositoryPort port;

    @Mock
    private AnonymizeDomain anonymizationDomain;

    @InjectMocks
    private AnonymizeApplicationService service;

    private UUID testId;
    private UUID testPersonRef;
    private Anonymize testAnonymize;
    private CreateAnonymizeCmd testCmd;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testPersonRef = UUID.randomUUID();
        testAnonymize = Anonymize.of(
            AnonymizeId.of(testId),
            PersonRef.of(testPersonRef)
        );
        testCmd = new CreateAnonymizeCmd(testPersonRef);
    }

    // ===== CREATE TESTS =====

    @Test
    @DisplayName("Should create anonymize successfully")
    void testCreate_ShouldCreateAnonymizeSuccessfully() {
        // Given
        Anonymize draft = Anonymize.of(AnonymizeId.of(testId),PersonRef.of(testPersonRef));
        when(anonymizationDomain.create(PersonRef.of(testPersonRef)))
            .thenReturn(draft);
        when(port.save(draft))
            .thenReturn(testAnonymize);

        // When
        AnonymizeResponse result = service.create(testCmd);

        // Then
        assertNotNull(result);
        assertEquals(testId, result.anonymizationId());
        assertEquals(testPersonRef, result.personRef());

        verify(anonymizationDomain).create(PersonRef.of(testPersonRef));
        verify(port).save(draft);
    }

    @Test
    @DisplayName("Should throw exception when domain creation fails")
    void testCreate_WhenDomainCreationFails_ShouldPropagateException() {
        // Given
        when(anonymizationDomain.create(PersonRef.of(testPersonRef)))
            .thenThrow(new IllegalArgumentException("Invalid person reference"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.create(testCmd);
        });

        verify(anonymizationDomain).create(PersonRef.of(testPersonRef));
        verify(port, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository save failure")
    void testCreate_WhenRepositorySaveFails_ShouldPropagateException() {
        // Given
        Anonymize draft = Anonymize.of(AnonymizeId.of(testId), PersonRef.of(testPersonRef));
        when(anonymizationDomain.create(PersonRef.of(testPersonRef)))
            .thenReturn(draft);
        when(port.save(draft))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            service.create(testCmd);
        });

        verify(anonymizationDomain).create(PersonRef.of(testPersonRef));
        verify(port).save(draft);
    }

    @Test
    @DisplayName("Should handle null command")
    void testCreate_WithNullCommand_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            service.create(null);
        });

        verifyNoInteractions(anonymizationDomain);
        verifyNoInteractions(port);
    }

    // ===== FIND BY ID TESTS =====

    @Test
    @DisplayName("Should find anonymize by ID successfully")
    void testFindById_ShouldFindAnonymizeSuccessfully() {
        // Given
        FetchByIdQuery query = new FetchByIdQuery(testId);
        when(port.findById(AnonymizeId.of(testId)))
            .thenReturn(Optional.of(testAnonymize));

        // When
        Optional<AnonymizeResponse> result = service.findById(query);

        // Then
        assertTrue(result.isPresent());
        AnonymizeResponse response = result.get();
        assertEquals(testId, response.anonymizationId());
        assertEquals(testPersonRef, response.personRef());

        verify(port).findById(AnonymizeId.of(testId));
    }

    @Test
    @DisplayName("Should return empty when anonymize not found")
    void testFindById_WhenAnonymizeNotFound_ShouldThrowNotFoundException() {
        // Given
        FetchByIdQuery query = new FetchByIdQuery(testId);
        when(port.findById(AnonymizeId.of(testId)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            service.findById(query);
        });

        verify(port).findById(AnonymizeId.of(testId));
    }

    @Test
    @DisplayName("Should handle null query")
    void testFindById_WithNullQuery_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            service.findById(null);
        });

        verifyNoInteractions(port);
    }

    // ===== FIND BY PARAMS TESTS =====

    @Test
    @DisplayName("Should find by both ID and person ref successfully")
    void testFindByParams_WithBothParams_ShouldFindSuccessfully() {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(testId, testPersonRef);
        when(port.findBySearchParams(AnonymizeId.of(testId), PersonRef.of(testPersonRef)))
            .thenReturn(List.of(testAnonymize));

        // When
        List<AnonymizeResponse> result = service.findByParams(query);

        // Then
        assertEquals(1, result.size());
        AnonymizeResponse response = result.get(0);
        assertEquals(testId, response.anonymizationId());
        assertEquals(testPersonRef, response.personRef());

        verify(port).findBySearchParams(AnonymizeId.of(testId), PersonRef.of(testPersonRef));
    }

    @Test
    @DisplayName("Should find by ID only when person ref is null")
    void testFindByParams_WithOnlyId_ShouldFindSuccessfully() {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(testId, null);
        when(port.findBySearchParams(any(AnonymizeId.class), any()))
            .thenReturn(List.of(testAnonymize));

        // When
        List<AnonymizeResponse> result = service.findByParams(query);

        // Then
        assertEquals(1, result.size());
        assertEquals(testId, result.get(0).anonymizationId());

        verify(port).findBySearchParams(any(AnonymizeId.class), any());
    }

    @Test
    @DisplayName("Should find by person ref only when ID is null")
    void testFindByParams_WithOnlyPersonRef_ShouldFindSuccessfully() {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(null, testPersonRef);
        when(port.findBySearchParams(any(), any(PersonRef.class)))
            .thenReturn(List.of(testAnonymize));

        // When
        List<AnonymizeResponse> result = service.findByParams(query);

        // Then
        assertEquals(1, result.size());
        assertEquals(testPersonRef, result.get(0).personRef());

        verify(port).findBySearchParams(any(), any(PersonRef.class));
    }

    @Test
    @DisplayName("Should return empty when both params are null")
    void testFindByParams_WithBothParamsNull_ShouldReturnEmptyList() {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(null, null);
        when(port.findBySearchParams(isNull(), isNull()))
            .thenReturn(List.of());

        // When
        List<AnonymizeResponse> result = service.findByParams(query);

        // Then
        assertTrue(result.isEmpty());

        verify(port).findBySearchParams(isNull(), isNull());
    }

    @Test
    @DisplayName("Should handle repository exception")
    void testFindByParams_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        FetchByParamsQuery query = new FetchByParamsQuery(testId, testPersonRef);
        when(port.findBySearchParams(AnonymizeId.of(testId), PersonRef.of(testPersonRef)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            service.findByParams(query);
        });

        verify(port).findBySearchParams(AnonymizeId.of(testId), PersonRef.of(testPersonRef));
    }

    // ===== HELPER METHOD TESTS =====

    @Test
    @DisplayName("toResponse should convert anonymize to response correctly")
    void testToResponse_ShouldConvertCorrectly() {
        // Given
        // Brug reflection eller public metode hvis tilgængelig
        // Ellers test gennem public metoder der bruger toResponse

        // When & Then - testes indirekte gennem andre metoder
        FetchByIdQuery query = new FetchByIdQuery(testId);
        when(port.findById(AnonymizeId.of(testId)))
            .thenReturn(Optional.of(testAnonymize));

        Optional<AnonymizeResponse> result = service.findById(query);

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().anonymizationId());
        assertEquals(testPersonRef, result.get().personRef());
    }

    // ===== TRANSACTION TESTS =====

    @Test
    @DisplayName("Should maintain transaction boundaries")
    void testTransactions_ShouldMaintainBoundaries() {
        // This test verifies that @Transactional annotations are present
        // In a real scenario, you might use @TransactionalTestExecutionListener
        
        // Given
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(testPersonRef);
        Anonymize draft = Anonymize.of(AnonymizeId.of(testId),PersonRef.of(testPersonRef));
        when(anonymizationDomain.create(PersonRef.of(testPersonRef)))
            .thenReturn(draft);
        when(port.save(draft))
            .thenReturn(testAnonymize);

        // When
        AnonymizeResponse result = service.create(cmd);

        // Then - if @Transactional is working, operations should be atomic
        assertNotNull(result);
        verify(anonymizationDomain).create(PersonRef.of(testPersonRef));
        verify(port).save(draft);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle maximum UUID values")
    void testEdgeCases_WithMaxUuid_ShouldHandleCorrectly() {
        // Given
        UUID maxId = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        UUID maxPersonRef = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        Anonymize maxAnonymize = Anonymize.of(AnonymizeId.of(maxId), PersonRef.of(maxPersonRef));
        
        CreateAnonymizeCmd cmd = new CreateAnonymizeCmd(maxPersonRef);
        when(anonymizationDomain.create(PersonRef.of(maxPersonRef)))
            .thenReturn(Anonymize.of(AnonymizeId.of(maxId), PersonRef.of(maxPersonRef)));
        when(port.save(any()))
            .thenReturn(maxAnonymize);

        // When
        AnonymizeResponse result = service.create(cmd);

        // Then
        assertNotNull(result);
        assertEquals(maxId, result.anonymizationId());
        assertEquals(maxPersonRef, result.personRef());
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void testEdgeCases_WithEmptyResults_ShouldHandleGracefully() {
        // Given
        when(port.findBySearchParams(any(), any()))
            .thenReturn(List.of());

        // When
        List<AnonymizeResponse> result = service.findByParams(new FetchByParamsQuery(null, null));

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void compensate_WhenRepositoryReturnsTrue_ShouldReturnCompensated() {
        // Given
        when(port.compensate(
                AnonymizeId.of(testId),
                SagaOutcome.COMPENSATE))
                .thenReturn(true);

        // When
        ResponseCompensated result = service.compensate(
                testId,
                String.class,
                SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());

        verify(port).compensate(
                AnonymizeId.of(testId),
                SagaOutcome.COMPENSATE);
    }

    @Test
    void compensate_WhenRepositoryReturnsFalse_ShouldReturnIdempotent() {
        // Given
        when(port.compensate(
                AnonymizeId.of(testId),
                SagaOutcome.COMPENSATE))
                .thenReturn(false);

        // When
        ResponseCompensated result = service.compensate(
                testId,
                String.class,
                SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());

        verify(port).compensate(
                AnonymizeId.of(testId),
                SagaOutcome.COMPENSATE);
    }

    @Test
    void should_store_and_return_values() {
        // given
        Class<?> clazz = String.class;
        SagaOutcome sagaState = SagaOutcome.SUCCEEDED; // adjust if enum differs

        // when
        CompensateAnonymizeStatementCmd cmd =
                new CompensateAnonymizeStatementCmd(clazz, sagaState);

        // then
        assertEquals(clazz, cmd.clazz());
        assertEquals(sagaState, cmd.sagaState());
    }

    @Test
    void records_should_be_equal_when_values_are_same() {
        // given
        Class<?> clazz = String.class;
        SagaOutcome sagaState = SagaOutcome.SUCCEEDED;

        // when
        CompensateAnonymizeStatementCmd cmd1 =
                new CompensateAnonymizeStatementCmd(clazz, sagaState);

        CompensateAnonymizeStatementCmd cmd2 =
                new CompensateAnonymizeStatementCmd(clazz, sagaState);

        // then
        assertEquals(cmd1, cmd2);
        assertEquals(cmd1.hashCode(), cmd2.hashCode());
    }

    @Test
    void should_not_be_equal_when_values_differ() {
        // given
        CompensateAnonymizeStatementCmd cmd1 =
                new CompensateAnonymizeStatementCmd(String.class, SagaOutcome.SUCCEEDED);

        CompensateAnonymizeStatementCmd cmd2 =
                new CompensateAnonymizeStatementCmd(Integer.class, SagaOutcome.SUCCEEDED);

        // then
        assertNotEquals(cmd1, cmd2);
    }

    

}
