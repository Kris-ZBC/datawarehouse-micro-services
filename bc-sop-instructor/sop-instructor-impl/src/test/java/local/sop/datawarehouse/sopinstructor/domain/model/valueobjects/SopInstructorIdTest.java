package local.sop.datawarehouse.sopinstructor.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SopInstructorIdTest {

    @Test
    void shouldCreateSopInstructorId_whenBothRefsAreValid() {
        SopRef sopRef = SopRef.newId();
        InstructorRef instructorRef = InstructorRef.newId();
        SopInstructorId id = new SopInstructorId(sopRef, instructorRef);
        assertEquals(sopRef, id.sopRef());
        assertEquals(instructorRef, id.instructorRef());
    }

    @Test
    void shouldThrow_whenSopRefIsNull() {
        InstructorRef instructorRef = InstructorRef.newId();
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new SopInstructorId(null, instructorRef));
        assertEquals("sopRef is required", ex.getMessage());
    }

    @Test
    void shouldThrow_whenInstructorRefIsNull() {
        SopRef sopRef = SopRef.newId();
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new SopInstructorId(sopRef, null));
        assertEquals("instructorRef is required", ex.getMessage());
    }

}
