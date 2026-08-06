package local.sop.datawarehouse.apprentice.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonRefTest {

    @Test
    @DisplayName("Should create PersonRef when UUID is provided")
    void constructor_WithValidUuid_ShouldCreateInstance() {
        // Arrange
        UUID expectedUuid = UUID.randomUUID();

        // Act
        PersonRef ref = new PersonRef(expectedUuid);

        // Assert
        assertThat(ref.value()).isEqualTo(expectedUuid);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when UUID is null")
    void constructor_WithNullValue_ShouldThrowException() {
        assertThatThrownBy(() -> new PersonRef(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PersonRef cannot be null");
    }

    @Test
    @DisplayName("Should generate a new PersonRef via static factory")
    void newId_ShouldReturnValidInstance() {
        // Act
        PersonRef ref = PersonRef.newId();

        // Assert
        assertThat(ref).isNotNull();
        assertThat(ref.value()).isNotNull();
    }

    @Test
    @DisplayName("Should return the string representation of the underlying UUID")
    void toString_ShouldReturnUuidString() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        PersonRef ref = new PersonRef(uuid);

        // Act & Assert
        assertThat(ref.toString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("Should verify Record equality and hashCode contracts")
    void equalsAndHashCode_ShouldBehaveCorrectly() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        PersonRef ref1 = new PersonRef(uuid);
        PersonRef ref2 = new PersonRef(uuid);
        PersonRef ref3 = new PersonRef(UUID.randomUUID());

        // Assert
        assertThat(ref1).isEqualTo(ref2);
        assertThat(ref1.hashCode()).isEqualTo(ref2.hashCode());
        assertThat(ref1).isNotEqualTo(ref3);
        assertThat(ref1).isNotEqualTo(null);
        assertThat(ref1).isNotEqualTo(new Object());
    }
}