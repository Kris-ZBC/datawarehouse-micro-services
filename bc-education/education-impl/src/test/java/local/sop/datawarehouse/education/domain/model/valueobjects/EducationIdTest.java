package local.sop.datawarehouse.education.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class EducationIdTest {

    @Test
    void shouldCreateEducationIdWithValidUUID() {
        UUID uuid = UUID.randomUUID();
        EducationId id = new EducationId(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> new EducationId(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldCreateNewUniqueId() {
        EducationId id1 = EducationId.newId();
        EducationId id2 = EducationId.newId();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldParseValidUUIDString() {
        UUID uuid = UUID.randomUUID();
        EducationId id = EducationId.parse(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowWhenParsingInvalidString() {
        assertThatThrownBy(() -> EducationId.parse("not-a-uuid"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenParsingNull() {
        assertThatThrownBy(() -> EducationId.parse(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldReturnStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        EducationId id = new EducationId(uuid);
        assertThat(id.asString()).isEqualTo(uuid.toString());
    }

    @Test
    void shouldBeEqualWhenSameUUID() {
        UUID uuid = UUID.randomUUID();
        EducationId id1 = new EducationId(uuid);
        EducationId id2 = new EducationId(uuid);
        assertThat(id1).isEqualTo(id2);
    }

    @Test
    void shouldReturnStringRepresentation1() {
        UUID uuid = UUID.randomUUID();
        EducationId id = new EducationId(uuid);
        assertThat(id.asString()).isEqualTo(uuid.toString());
    }

    @Test
    void shouldReturnMissingException() {
        EducationId id = EducationId.newId();
        ValidationException exception = id.missingException();
        
        assertThat(exception.getMessage()).isEqualTo("education.id.required");
        assertThat(exception).isNotNull();
    }
}