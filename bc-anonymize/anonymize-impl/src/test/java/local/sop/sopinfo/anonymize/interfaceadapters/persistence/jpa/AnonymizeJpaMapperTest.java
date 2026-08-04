package local.sop.sopinfo.anonymize.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@ExtendWith(MockitoExtension.class)
public class AnonymizeJpaMapperTest {

    @InjectMocks
    private AnonymizeJpaMapper mapper;

        private static final UUID TEST_ID = UUID.randomUUID();
    private static final UUID TEST_PERSON_REF = UUID.randomUUID();

    // ===== toDomain TESTS =====

    @Test
    @DisplayName("Should convert entity to domain object successfully")
    void testToDomain_ShouldConvertEntityToDomain() {
        // Given
        AnonymizeEntity entity = AnonymizeEntity.create(TEST_ID, TEST_PERSON_REF);

        // When - kald instance metode
        Anonymize result = mapper.toDomain(entity);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ID, result.getAnonymizationId().value());
        assertEquals(TEST_PERSON_REF, result.getPersonRef().value());
    }

    @Test
    @DisplayName("Should handle null entity gracefully")
    void testToDomain_WithNullEntity_ShouldThrowException() {
        // When & Then - instance metode skal håndtere null
        assertThrows(NullPointerException.class, () -> {
            mapper.toDomain(null);
        });
    }

    @Test
    @DisplayName("Should handle entity with null fields")
    void testToDomain_WithNullFields_ShouldThrowException() {
        // Given
        AnonymizeEntity entity = new AnonymizeEntity();

        // When & Then
        assertThrows(ValidationException.class, () -> {
            mapper.toDomain(entity);
        });
    }

    // ===== toEntity TESTS =====

    @Test
    @DisplayName("Should convert domain object to entity successfully")
    void testToEntity_ShouldConvertDomainToEntity() {
        // Given
        Anonymize anonymize = Anonymize.of(
            AnonymizeId.of(TEST_ID),
            PersonRef.of(TEST_PERSON_REF)
        );

        // When 
        AnonymizeEntity result = mapper.toEntity(anonymize);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_PERSON_REF, result.getPersonRef());
    }

    @Test
    @DisplayName("Should handle null domain object gracefully")
    void testToEntity_WithNullDomain_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            mapper.toEntity(null);
        });
    }

    // ===== ROUND-TRIP TESTS =====

    @Test
    @DisplayName("Should maintain data integrity through round-trip conversion")
    void testRoundTripConversion_ShouldMaintainDataIntegrity() {
        // Given
        Anonymize original = Anonymize.of(
            AnonymizeId.of(TEST_ID),
            PersonRef.of(TEST_PERSON_REF)
        );

        // When
        AnonymizeEntity entity = mapper.toEntity(original);
        Anonymize convertedBack = mapper.toDomain(entity);

        // Then
        assertEquals(original.getAnonymizationId().value(), convertedBack.getAnonymizationId().value());
        assertEquals(original.getPersonRef().value(), convertedBack.getPersonRef().value());
    }

    // ===== BOUNDARY VALUE TESTS =====

    @Test
    @DisplayName("Should handle maximum UUID values")
    void testBoundaryValues_WithMaxUuid_ShouldConvertCorrectly() {
        // Given
        UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        Anonymize anonymize = Anonymize.of(
            AnonymizeId.of(maxUuid),
            PersonRef.of(TEST_PERSON_REF)
        );

        // When
        AnonymizeEntity entity = mapper.toEntity(anonymize);
        Anonymize result = mapper.toDomain(entity);

        // Then
        assertEquals(maxUuid, result.getAnonymizationId().value());
    }

    // ===== CONSISTENCY TESTS =====

    @Test
    @DisplayName("Should produce consistent results for multiple conversions")
    void testConsistency_ShouldProduceSameResultForMultipleConversions() {
        // Given
        Anonymize anonymize = Anonymize.of(
            AnonymizeId.of(TEST_ID),
            PersonRef.of(TEST_PERSON_REF)
        );

        // When
        Anonymize result1 = mapper.toDomain(
            mapper.toEntity(anonymize)
        );
        Anonymize result2 = mapper.toDomain(
            mapper.toEntity(anonymize)
        );

        // Then
        assertEquals(result1.getAnonymizationId(), result2.getAnonymizationId());
        assertEquals(result1.getPersonRef(), result2.getPersonRef());
    }
}
