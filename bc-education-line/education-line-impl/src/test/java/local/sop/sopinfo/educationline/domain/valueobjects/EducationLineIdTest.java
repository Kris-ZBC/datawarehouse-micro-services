package local.sop.sopinfo.educationline.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class EducationLineIdTest {

    @Test
    void happyPath_newId_returns_id() {
        EducationLineId id = EducationLineId.newId();
        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void happyPath_newId_returns_unique() {
        EducationLineId id1 = EducationLineId.newId();
        EducationLineId id2 = EducationLineId.newId();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void happyPath_parse_returns_id() {
        String uuid = UUID.randomUUID().toString();
        EducationLineId id = EducationLineId.parse(uuid);
        assertEquals(uuid, id.value().toString());
    }

    @Test
    void happyPath_asString_returns_uuid_string() {
        String uuid = UUID.randomUUID().toString();
        EducationLineId id = EducationLineId.parse(uuid);
        assertEquals(uuid, id.asString());
    }

    @Test
    void unhappyPath_null_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineId(null));
    }

    @Test
    void unhappyPath_invalid_uuid_fails() {
        assertThrows(ValidationException.class, () -> EducationLineId.parse("not-a-uuid"));
    }

    @Test
    void unhappyPath_missingException_returns_expected() {
        EducationLineId id = EducationLineId.newId();
        ValidationException ex = id.missingException();
        assertEquals("educationline.id.required", ex.getMessage());
    }
}
