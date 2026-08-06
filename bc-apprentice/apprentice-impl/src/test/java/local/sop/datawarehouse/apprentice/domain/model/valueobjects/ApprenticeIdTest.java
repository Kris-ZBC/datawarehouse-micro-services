package local.sop.datawarehouse.apprentice.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprenticeIdTest {

    @Test
    @DisplayName("Should create ApprenticeId when value is valid")
    void constructor_WithValidUuid_ShouldCreateInstance() {
        // Arrange
        UUID uuid = UUID.randomUUID();

        // Act
        ApprenticeId apprenticeId = new ApprenticeId(uuid);

        // Assert
        assertThat(apprenticeId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when value is null")
    void constructor_WithNullValue_ShouldThrowException() {
        assertThatThrownBy(() -> new ApprenticeId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ApprenticeId cannot be null");
    }

    @Test
    @DisplayName("Should generate a new valid ApprenticeId")
    void newApprenticeId_ShouldReturnValidInstance() {
        // Act
        ApprenticeId apprenticeId = ApprenticeId.newId();

        // Assert
        assertThat(apprenticeId).isNotNull();
        assertThat(apprenticeId.value()).isNotNull();
    }

    @Test
    @DisplayName("Should return string representation of the UUID")
    void toString_ShouldReturnUuidString() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ApprenticeId apprenticeId = new ApprenticeId(uuid);

        // Act & Assert
        assertThat(apprenticeId.toString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("Should satisfy equality and hashcode contracts")
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ApprenticeId id1 = new ApprenticeId(uuid);
        ApprenticeId id2 = new ApprenticeId(uuid);
        ApprenticeId id3 = new ApprenticeId(UUID.randomUUID());

        // Assert
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1).isNotEqualTo(null);
        assertThat(id1).isNotEqualTo("some string");
    }
}