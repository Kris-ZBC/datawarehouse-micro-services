package local.sop.sopinfo.educationline.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class EducationRefTest {

    @Test
    void happyPath_valid_uuid_succeeds() {
        UUID uuid = UUID.randomUUID();
        EducationRef ref = new EducationRef(uuid);
        assertEquals(uuid, ref.value());
        assertEquals(uuid.toString(), ref.asString());
    }

    @Test
    void happyPath_newEducationRef_succeeds() {
        EducationRef ref = EducationRef.newEducationRef();
        assertNotNull(ref.value());
        assertNotNull(ref.asString());
    }

    @Test
    void unhappyPath_null_fails() {
        assertThrows(ValidationException.class, () -> new EducationRef(null));
    }
}
