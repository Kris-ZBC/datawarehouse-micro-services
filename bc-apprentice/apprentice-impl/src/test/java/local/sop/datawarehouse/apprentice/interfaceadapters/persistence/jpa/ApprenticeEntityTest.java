package local.sop.datawarehouse.apprentice.interfaceadapters.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApprenticeEntityTest {

    private final UUID id = UUID.randomUUID();
    private final UUID personRef = UUID.randomUUID();
    private final UUID educationLineRef = UUID.randomUUID();

    @Test
    @DisplayName("Should create entity via parameterized constructor and return values via getters")
    void constructorAndGetters_ShouldWorkCorrectly() {
        // Act
        ApprenticeEntity entity = new ApprenticeEntity(id, personRef, educationLineRef);

        // Assert
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getPersonRef()).isEqualTo(personRef);
        assertThat(entity.getEducationLineRef()).isEqualTo(educationLineRef);
        assertThat(entity.getVersion()).isNull(); // Version is managed by JPA, null on new object
    }

    @Test
    @DisplayName("Should support default constructor for JPA")
    void defaultConstructor_ShouldExist() {
        // Act
        ApprenticeEntity entity = new ApprenticeEntity();

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("Should create new instance with updated ID using withId")
    void withId_ShouldReturnNewInstance() {
        // Arrange
        ApprenticeEntity original = new ApprenticeEntity(id, personRef, educationLineRef);
        UUID newId = UUID.randomUUID();

        // Act
        ApprenticeEntity updated = original.withId(newId);

        // Assert
        assertThat(updated.getId()).isEqualTo(newId);
        assertThat(updated.getPersonRef()).isEqualTo(personRef);
        assertThat(updated).isNotSameAs(original);
    }

    @Test
    @DisplayName("Should create new instance with updated PersonRef using withPersonRef")
    void withPersonRef_ShouldReturnNewInstance() {
        // Arrange
        ApprenticeEntity original = new ApprenticeEntity(id, personRef, educationLineRef);
        UUID newPersonRef = UUID.randomUUID();

        // Act
        ApprenticeEntity updated = original.withPersonRef(newPersonRef);

        // Assert
        assertThat(updated.getPersonRef()).isEqualTo(newPersonRef);
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated).isNotSameAs(original);
    }

    @Test
    @DisplayName("Should create new instance with updated EducationLineRef using withEducationLineRef")
    void withEducationLineRef_ShouldReturnNewInstance() {
        // Arrange
        ApprenticeEntity original = new ApprenticeEntity(id, personRef, educationLineRef);
        UUID newEduRef = UUID.randomUUID();

        // Act
        ApprenticeEntity updated = original.withEducationLineRef(newEduRef);

        // Assert
        assertThat(updated.getEducationLineRef()).isEqualTo(newEduRef);
        assertThat(updated.getPersonRef()).isEqualTo(personRef);
        assertThat(updated).isNotSameAs(original);
    }
}