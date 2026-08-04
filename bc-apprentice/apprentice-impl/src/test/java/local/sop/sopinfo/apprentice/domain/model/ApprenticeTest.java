package local.sop.sopinfo.apprentice.domain.model;

import local.sop.sopinfo.apprentice.domain.model.valueobjects.*;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprenticeTest {

    private final ApprenticeId apprenticeId = ApprenticeId.newId();
    private final PersonRef personRef = PersonRef.newId();
    private final EducationLineRef educationLineRef = EducationLineRef.newId();

    @Test
    @DisplayName("Should create apprentice with all fields via factory")
    void create_WithAllFields_ShouldSucceed() {
        Apprentice apprentice = Apprentice.builder()
                .id(apprenticeId)
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();

        assertThat(apprentice.getApprenticeId()).isEqualTo(apprenticeId);
        assertThat(apprentice.getPersonRef()).isEqualTo(personRef);
        assertThat(apprentice.getEducationLineRef()).isEqualTo(educationLineRef);
    }

    @Test
    @DisplayName("Should generate new ID when null ID is passed to create")
    void create_WithNullId_ShouldGenerateNewId() {
        Apprentice apprentice = Apprentice.builder()
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();

        assertThat(apprentice.getApprenticeId()).isNotNull();
        assertThat(apprentice.getPersonRef()).isEqualTo(personRef);
    }

    @Test
    @DisplayName("Should throw ValidationException when personRef is missing")
    void create_WithNullPersonRef_ShouldThrowException() {
        assertThatThrownBy(() -> Apprentice.builder()
                .id(apprenticeId)
                .educationLineRef(educationLineRef)
                .build())
                .isInstanceOf(ValidationException.class)
                .hasMessage("personRef.invalid");
    }

    @Test
    @DisplayName("Should throw ValidationException when educationLineRef is missing")
    void create_WithNullEducationLineRef_ShouldThrowException() {
        assertThatThrownBy(() -> Apprentice.builder()
                .id(apprenticeId)
                .personRef(personRef)
                .build())
                .isInstanceOf(ValidationException.class)
                .hasMessage("educationLineRef.invalid");
    }

    @Test
    @DisplayName("Should create new instance with updated ApprenticeId using wither")
    void withApprenticeId_ShouldReturnNewInstance() {
        Apprentice original = Apprentice.builder()
                .id(apprenticeId)
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();
        ApprenticeId newId = ApprenticeId.newId();

        Apprentice updated = original.withApprenticeId(newId);

        assertThat(updated.getApprenticeId()).isEqualTo(newId);
        assertThat(updated.getPersonRef()).isEqualTo(personRef);
        assertThat(original.getApprenticeId()).isEqualTo(apprenticeId);
    }

    @Test
    @DisplayName("Should create new instance with updated PersonRef using wither")
    void withPersonRef_ShouldReturnNewInstance() {
        Apprentice original = Apprentice.builder()
                .id(apprenticeId)
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();
        PersonRef newRef = PersonRef.newId();

        Apprentice updated = original.withPersonRef(newRef);

        assertThat(updated.getPersonRef()).isEqualTo(newRef);
        assertThat(updated.getApprenticeId()).isEqualTo(apprenticeId);
        assertThat(original.getPersonRef()).isEqualTo(personRef);
    }

    @Test
    @DisplayName("Should create new instance with updated EducationLineRef using wither")
    void withEducationLineRef_ShouldReturnNewInstance() {
        Apprentice original = Apprentice.builder()
                .id(apprenticeId)
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();
        EducationLineRef newRef = EducationLineRef.newId();

        Apprentice updated = original.withEducationLineRef(newRef);

        assertThat(updated.getEducationLineRef()).isEqualTo(newRef);
        assertThat(original.getEducationLineRef()).isEqualTo(educationLineRef);
    }

    @Test
    @DisplayName("Should produce a string representation containing field values")
    void toString_ShouldReturnFormattedString() {
        Apprentice apprentice = Apprentice.builder()
                .id(apprenticeId)
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();

        String result = apprentice.toString();

        assertThat(result)
                .contains("Apprentice")
                .contains(apprenticeId.value().toString())
                .contains(personRef.value().toString())
                .contains(educationLineRef.value().toString());
    }

    @Test
    @DisplayName("toString should handle null fields for full branch coverage")
    void toString_WithNullFields_ShouldHandleGracefully() {
        // 1. Create a valid apprentice first
        Apprentice apprentice = Apprentice.builder()
                .personRef(personRef)
                .educationLineRef(educationLineRef)
                .build();

        // 2. Use 'withers' to force null values to trigger the other ternary branches
        // (Note: This is why branch coverage is tricky—it forces you to test 
        // states that the Builder usually prevents!)
        Apprentice withNulls = apprentice
                .withApprenticeId(null)
                .withPersonRef(null)
                .withEducationLineRef(null);

        String result = withNulls.toString();

        // 3. Assertions to ensure it didn't crash and hit all branches
        assertThat(result)
                .contains("apprenticeId: null")
                .contains("personRef: null")
                .contains("educationLineRef: null");
    }
}