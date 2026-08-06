package local.sop.datawarehouse.educationline.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.educationline.domain.model.valueobjects.EducationLineCreatedAt;

public class EducationLineCreatedAtTest {

    @Test
    void happyPath_valid_instant_succeeds() {
        Instant now = Instant.now();
        EducationLineCreatedAt createdAt = new EducationLineCreatedAt(now);
        assertEquals(now, createdAt.value());
    }

    @Test
    void happyPath_now_factory_succeeds() {
        EducationLineCreatedAt createdAt = EducationLineCreatedAt.now();
        assertNotNull(createdAt.value());
    }

    @Test
    void unhappyPath_null_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineCreatedAt(null));
    }

    @Test
    void happyPath_toString_returns_value_string() {
        Instant now = Instant.now();
        EducationLineCreatedAt createdAt = new EducationLineCreatedAt(now);
        assertEquals(now.toString(), createdAt.toString());
    }
}
