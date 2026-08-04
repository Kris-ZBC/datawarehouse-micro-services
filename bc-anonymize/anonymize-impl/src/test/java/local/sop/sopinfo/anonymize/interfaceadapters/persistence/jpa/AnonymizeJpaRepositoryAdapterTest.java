package local.sop.sopinfo.anonymize.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test",
    "security.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AnonymizeJpaRepositoryAdapterTest {

    private AnonymizeJpaRepositoryAdapter adapter;

    @Autowired
    private AnonymizeSpringDataRepository repository;

    private AnonymizeJpaMapper mapper = new AnonymizeJpaMapper();

    private UUID testId1;
    private UUID testId2;
    private UUID testPersonRef1;
    private UUID testPersonRef2;
    private Anonymize testAnonymize1;
    private Anonymize testAnonymize2;

    @BeforeEach
    void setUp() {
        adapter = new AnonymizeJpaRepositoryAdapter(repository, mapper);        
        testId1 = UUID.randomUUID();
        testId2 = UUID.randomUUID();
        testPersonRef1 = UUID.randomUUID();
        testPersonRef2 = UUID.randomUUID();

        testAnonymize1 = Anonymize.of(
            AnonymizeId.of(testId1),
            PersonRef.of(testPersonRef1)
        );

        testAnonymize2 = Anonymize.of(
            AnonymizeId.of(testId2),
            PersonRef.of(testPersonRef2)
        );
    }

    // ===== SAVE TESTS =====

    @Test
    @DisplayName("Should save anonymize successfully")
    void testSave_ShouldSaveAnonymizeSuccessfully() {
        // When
        Anonymize result = adapter.save(testAnonymize1);

        // Then
        assertNotNull(result);
        assertEquals(testId1, result.getAnonymizationId().value());
        assertEquals(testPersonRef1, result.getPersonRef().value());

        // Verify database state
        Optional<Anonymize> found = adapter.findById(AnonymizeId.of(testId1));
        assertTrue(found.isPresent());
        assertEquals(result.getAnonymizationId(), found.get().getAnonymizationId());
        assertEquals(result.getPersonRef(), found.get().getPersonRef());
    }

    @Test
    @DisplayName("Should save multiple anonymizes successfully")
    void testSave_ShouldSaveMultipleAnonymizesSuccessfully() {
        // Given
        adapter.save(testAnonymize1);
        adapter.save(testAnonymize2);

        // When
        List<Anonymize> all = adapter.findAll();

        // Then
        assertEquals(2, all.size());
        // Use value-based comparison instead of contains()
        assertTrue(all.stream().anyMatch(a -> 
            a.getAnonymizationId().equals(testAnonymize1.getAnonymizationId()) &&
            a.getPersonRef().equals(testAnonymize1.getPersonRef())
        ));
        
        assertTrue(all.stream().anyMatch(a -> 
            a.getAnonymizationId().equals(testAnonymize2.getAnonymizationId()) &&
            a.getPersonRef().equals(testAnonymize2.getPersonRef())
        ));
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void testSave_WithNullInput_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            adapter.save(null);
        });
    }

    @Test
    @DisplayName("Should handle duplicate ID constraint violation")
    void testSave_WithDuplicateId_ShouldThrowException() {
        // Given
        adapter.save(testAnonymize1);

        // Create anonymize with same ID but different personRef
        Anonymize duplicate = Anonymize.of(
            AnonymizeId.of(testId1), // Same ID
            PersonRef.of(UUID.randomUUID()) // Different personRef
        );

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            adapter.save(duplicate);
        });
    }

    // ===== FIND BY ID TESTS =====

    @Test
    @DisplayName("Should find anonymize by ID successfully")
    void testFindById_ShouldFindAnonymizeSuccessfully() {
        // Given
        adapter.save(testAnonymize1);

        // When
        Optional<Anonymize> result = adapter.findById(AnonymizeId.of(testId1));

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAnonymize1.getAnonymizationId(), result.get().getAnonymizationId());
    }

    @Test
    @DisplayName("Should return empty when anonymize not found")
    void testFindById_WithNonExistentId_ShouldReturnEmpty() {
        // When
        Optional<Anonymize> result = adapter.findById(AnonymizeId.of(UUID.randomUUID()));

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle null ID gracefully")
    void testFindById_WithNullId_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            adapter.findById(null);
        });
    }

    // ===== FIND BY SEARCH PARAMS TESTS =====

    @Test
    @DisplayName("Should find by both ID and person ref successfully")
    void testFindBySearchParams_WithBothParams_ShouldFindSuccessfully() {
        // Given
        adapter.save(testAnonymize1);
        adapter.save(testAnonymize2);

        // When
        List<Anonymize> result = adapter.findBySearchParams(
            AnonymizeId.of(testId1),
            PersonRef.of(testPersonRef1)
        );

        // Then
        assertEquals(1, result.size());
        assertEquals(testAnonymize1.getAnonymizationId(), result.get(0).getAnonymizationId());
        assertEquals(testAnonymize1.getPersonRef(), result.get(0).getPersonRef());
    }

    @Test
    @DisplayName("Should find by ID only")
    void testFindBySearchParams_WithOnlyId_ShouldFindSuccessfully() {
        // Given
        adapter.save(testAnonymize1);
        Anonymize otherAnonymize = Anonymize.of(
            AnonymizeId.of(UUID.randomUUID()),
            PersonRef.of(testPersonRef1) // Same personRef, different ID
        );
        adapter.save(otherAnonymize);

        // When
        List<Anonymize> result = adapter.findBySearchParams(
            AnonymizeId.of(testId1),
            PersonRef.of(UUID.randomUUID()) // Different personRef
        );

        // Then
        assertEquals(1, result.size());
        assertEquals(testAnonymize1.getAnonymizationId(), result.get(0).getAnonymizationId());
        assertEquals(testAnonymize1.getPersonRef(), result.get(0).getPersonRef());
    }

    @Test
    @DisplayName("Should find by person ref only")
    void testFindBySearchParams_WithOnlyPersonRef_ShouldFindSuccessfully() {
        // Given
        adapter.save(testAnonymize1);
        Anonymize otherAnonymize = Anonymize.of(
            AnonymizeId.of(UUID.randomUUID()),
            PersonRef.of(testPersonRef1) // Same personRef
        );
        adapter.save(otherAnonymize);

        // When
        List<Anonymize> result = adapter.findBySearchParams(
            AnonymizeId.of(UUID.randomUUID()), // Different ID
            PersonRef.of(testPersonRef1)
        );

        // Then
        assertEquals(2, result.size()); // Should find both with same personRef
    }

    @Test
    @DisplayName("Should return empty when no matches found")
    void testFindBySearchParams_WithNoMatches_ShouldReturnEmpty() {
        // Given
        adapter.save(testAnonymize1);

        // When
        List<Anonymize> result = adapter.findBySearchParams(
            AnonymizeId.of(UUID.randomUUID()),
            PersonRef.of(UUID.randomUUID())
        );

        // Then
        assertTrue(result.isEmpty());
    }

    // ===== FIND ALL TESTS =====

    @Test
    @DisplayName("Should find all anonymizes successfully")
    void testFindAll_ShouldFindAllAnonymizesSuccessfully() {
        // Given
        adapter.save(testAnonymize1);
        adapter.save(testAnonymize2);

        // When
        List<Anonymize> result = adapter.findAll();

        // Then
        assertTrue(result.stream().anyMatch(a -> 
            a.getAnonymizationId().equals(testAnonymize1.getAnonymizationId()) &&
            a.getPersonRef().equals(testAnonymize1.getPersonRef())
        ));
        
        assertTrue(result.stream().anyMatch(a -> 
            a.getAnonymizationId().equals(testAnonymize2.getAnonymizationId()) &&
            a.getPersonRef().equals(testAnonymize2.getPersonRef())
        ));
    }

    @Test
    @DisplayName("Should return empty list when no anonymizes exist")
    void testFindAll_WithNoAnonymizes_ShouldReturnEmptyList() {
        // When
        List<Anonymize> result = adapter.findAll();

        // Then
        assertTrue(result.isEmpty());
    }

    // ===== ROUND-TRIP TESTS =====

    @Test
    @DisplayName("Should maintain data integrity through save-find cycle")
    void testRoundTrip_ShouldMaintainDataIntegrity() {
        // When
        Anonymize saved = adapter.save(testAnonymize1);
        Optional<Anonymize> found = adapter.findById(AnonymizeId.of(testId1));

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getAnonymizationId(), found.get().getAnonymizationId());
        assertEquals(testAnonymize1.getAnonymizationId(), found.get().getAnonymizationId());
        assertEquals(testAnonymize1.getPersonRef(), found.get().getPersonRef());
    }

    // ===== BOUNDARY VALUE TESTS =====

    @Test
    @DisplayName("Should handle maximum UUID values")
    void testBoundaryValues_WithMaxUuid_ShouldHandleCorrectly() {
        // Given
        UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        Anonymize maxAnonymize = Anonymize.of(
            AnonymizeId.of(maxUuid),
            PersonRef.of(UUID.randomUUID())
        );
        adapter.save(maxAnonymize);

        // When
        Optional<Anonymize> found = adapter.findById(AnonymizeId.of(maxUuid));

        // Then
        assertTrue(found.isPresent());
        assertEquals(maxUuid, found.get().getAnonymizationId().value());
    }

    @Test
    @DisplayName("Should handle minimum UUID values")
    void testBoundaryValues_WithMinUuid_ShouldHandleCorrectly() {
        // Given
        UUID minUuid = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        Anonymize minAnonymize = Anonymize.of(
            AnonymizeId.of(minUuid),
            PersonRef.of(UUID.randomUUID())
        );

        // When
        adapter.save(minAnonymize);
        Optional<Anonymize> found = adapter.findById(AnonymizeId.of(minUuid));

        // Then
        assertTrue(found.isPresent());
        assertEquals(minUuid, found.get().getAnonymizationId().value());
    }

    // ===== CONSISTENCY TESTS =====

    @Test
    @DisplayName("Should produce consistent results across multiple operations")
    void testConsistency_ShouldProduceConsistentResults() {
        // Given
        adapter.save(testAnonymize1);
        adapter.save(testAnonymize2);

        // When
        List<Anonymize> all1 = adapter.findAll();
        List<Anonymize> all2 = adapter.findAll();

        // Then
        assertEquals(all1.size(), all2.size());
        assertTrue(all1.stream().allMatch(a1 -> 
            all2.stream().anyMatch(a2 -> 
                a1.getAnonymizationId().equals(a2.getAnonymizationId()) &&
                a1.getPersonRef().equals(a2.getPersonRef())
            )
        ));
        
        assertTrue(all2.stream().allMatch(a2 -> 
            all1.stream().anyMatch(a1 -> 
                a2.getAnonymizationId().equals(a1.getAnonymizationId()) &&
                a2.getPersonRef().equals(a1.getPersonRef())
            )
        ));
    }

    @Test
    void compensate_WhenSagaStateIsNotCompensate_ShouldThrowConflictException() {
        // When / Then
        assertThrows(
                ConflictException.class,
                () -> adapter.compensate(
                        AnonymizeId.of(testId1),
                        SagaOutcome.COMPENSATED));
    }

    @Test
    void compensate_WhenEntityDoesNotExist_ShouldReturnFalse() {
        // Given
        AnonymizeId nonExistingId = AnonymizeId.of(UUID.randomUUID());

        // When
        Boolean result = adapter.compensate(
                nonExistingId,
                SagaOutcome.COMPENSATE);

        // Then
        assertFalse(result);
    }

}
