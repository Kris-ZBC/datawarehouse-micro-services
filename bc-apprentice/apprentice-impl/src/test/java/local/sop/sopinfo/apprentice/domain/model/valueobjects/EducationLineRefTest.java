package local.sop.sopinfo.apprentice.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EducationLineRefTest {

    @Test
    @DisplayName("Should create EducationLineRef when UUID is provided")
    void constructor_WithValidUuid_ShouldCreateInstance() {
        // Arrange
        UUID expectedUuid = UUID.randomUUID();

        // Act
        EducationLineRef ref = new EducationLineRef(expectedUuid);

        // Assert
        assertThat(ref.value()).isEqualTo(expectedUuid);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when UUID is null")
    void constructor_WithNullValue_ShouldThrowException() {
        assertThatThrownBy(() -> new EducationLineRef(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("EducationLineRef cannot be null");
    }

    @Test
    @DisplayName("Should generate a new EducationLineRef via static factory")
    void newId_ShouldReturnValidInstance() {
        // Act
        EducationLineRef ref = EducationLineRef.newId();

        // Assert
        assertThat(ref).isNotNull();
        assertThat(ref.value()).isNotNull();
    }

    @Test
    @DisplayName("Should return the string representation of the underlying UUID")
    void toString_ShouldReturnUuidString() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        EducationLineRef ref = new EducationLineRef(uuid);

        // Act & Assert
        assertThat(ref.toString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("Should verify Record equality and hashCode contracts")
    void equalsAndHashCode_ShouldBehaveCorrectly() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        EducationLineRef ref1 = new EducationLineRef(uuid);
        EducationLineRef ref2 = new EducationLineRef(uuid);
        EducationLineRef ref3 = new EducationLineRef(UUID.randomUUID());

        // Assert
        assertThat(ref1).isEqualTo(ref2);
        assertThat(ref1.hashCode()).isEqualTo(ref2.hashCode());
        assertThat(ref1).isNotEqualTo(ref3);
        assertThat(ref1).isNotEqualTo(null);
        // Ensure it doesn't match a different type
        assertThat(ref1).isNotEqualTo("some-string");
    }
}