package local.sop.datawarehouse.apprentice.interfaceadapters.persistence.jpa.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UUIDBinaryConverterTest {

    private final UUIDBinaryConverter converter = new UUIDBinaryConverter();

    @Test
    @DisplayName("convertToDatabaseColumn: should return 16 bytes for a valid UUID")
    void convertToDatabaseColumn_WithValidUuid_ShouldReturnByteArray() {
        // Arrange
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // Act
        byte[] result = converter.convertToDatabaseColumn(uuid);

        // Assert
        assertThat(result).isNotNull().hasSize(16);
        // Verify bit order (Most Significant Bits followed by Least Significant Bits)
        // 550e8400-e29b-41d4 -> first 8 bytes
        assertThat(result[0]).isEqualTo((byte) 0x55); 
    }

    @Test
    @DisplayName("convertToDatabaseColumn: should return null when input is null")
    void convertToDatabaseColumn_WithNull_ShouldReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("convertToEntityAttribute: should return original UUID from valid 16 bytes")
    void convertToEntityAttribute_WithValidBytes_ShouldReturnUuid() {
        // Arrange
        UUID originalUuid = UUID.randomUUID();
        byte[] bytes = converter.convertToDatabaseColumn(originalUuid);

        // Act
        UUID result = converter.convertToEntityAttribute(bytes);

        // Assert
        assertThat(result).isEqualTo(originalUuid);
    }

    @Test
    @DisplayName("convertToEntityAttribute: should return null when input is null")
    void convertToEntityAttribute_WithNull_ShouldReturnNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 8, 15, 17, 32})
    @DisplayName("convertToEntityAttribute: should return null for invalid byte array lengths")
    void convertToEntityAttribute_WithInvalidLength_ShouldReturnNull(int length) {
        // Act
        UUID result = converter.convertToEntityAttribute(new byte[length]);

        // Assert
        assertThat(result).isNull();
    }
}