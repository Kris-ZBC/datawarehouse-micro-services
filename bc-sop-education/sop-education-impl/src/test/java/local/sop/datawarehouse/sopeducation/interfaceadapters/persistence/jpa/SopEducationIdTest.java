package local.sop.datawarehouse.sopeducation.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class SopEducationIdTest {

    private static final UUID SOP_REF             = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF   = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final UUID OTHER_SOP_REF        = UUID.fromString("333e4567-e89b-12d3-a456-426614174333");
    private static final UUID OTHER_EDU_REF   = UUID.fromString("444e4567-e89b-12d3-a456-426614174444");

    // ── Construction ───────────────────────────────────────────────────────────

    @Test
    void constructor_shouldSetBothRefs() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertEquals(SOP_REF, id.getSopRef());
        assertEquals(EDUCATION_REF, id.getEducationRef());
    }

    // ── Equality ───────────────────────────────────────────────────────────────

    @Test
    void equals_shouldReturnTrue_whenBothRefsAreEqual() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertEquals(id1, id2);
    }

    @Test
    void equals_shouldReturnTrue_whenSameInstance() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertEquals(id, id);
    }

    @Test
    void equals_shouldReturnFalse_whenSopRefDiffers() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(OTHER_SOP_REF, EDUCATION_REF);

        assertNotEquals(id1, id2);
    }

    @Test
    void equals_shouldReturnFalse_whenEducationLineRefDiffers() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(SOP_REF, OTHER_EDU_REF);

        assertNotEquals(id1, id2);
    }

    @Test
    void equals_shouldReturnFalse_whenBothRefsDiffer() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(OTHER_SOP_REF, OTHER_EDU_REF);

        assertNotEquals(id1, id2);
    }

    @Test
    void equals_shouldReturnFalse_whenComparedToNull() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertNotEquals(null, id);
    }

    @Test
    void equals_shouldReturnFalse_whenComparedToDifferentType() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertNotEquals(id, "not-an-id");
    }

    // ── HashCode ───────────────────────────────────────────────────────────────

    @Test
    void hashCode_shouldBeEqual_whenBothRefsAreEqual() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void hashCode_shouldDiffer_whenSopRefDiffers() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(OTHER_SOP_REF, EDUCATION_REF);

        assertNotEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void hashCode_shouldDiffer_whenEducationLineRefDiffers() {
        SopEducationId id1 = new SopEducationId(SOP_REF, EDUCATION_REF);
        SopEducationId id2 = new SopEducationId(SOP_REF, OTHER_EDU_REF);

        assertNotEquals(id1.hashCode(), id2.hashCode());
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    @Test
    void getSopRef_shouldReturnCorrectValue() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertEquals(SOP_REF, id.getSopRef());
    }

    @Test
    void getEducationLineRef_shouldReturnCorrectValue() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        assertEquals(EDUCATION_REF, id.getEducationRef());
    }

    // ── ToString ───────────────────────────────────────────────────────────────

    @Test
    void toString_shouldContainBothRefs() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        String result = id.toString();

        assertTrue(result.contains(SOP_REF.toString()));
        assertTrue(result.contains(EDUCATION_REF.toString()));
    }

    @Test
    void toString_shouldMatchExpectedFormat() {
        SopEducationId id = new SopEducationId(SOP_REF, EDUCATION_REF);

        String expected = "SopEducationId{sopRef=" + SOP_REF + ", educationRef=" + EDUCATION_REF + "}";

        assertEquals(expected, id.toString());
    }
}
